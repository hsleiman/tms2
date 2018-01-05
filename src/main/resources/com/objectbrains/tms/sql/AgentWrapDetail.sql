with stat_history as (
    select hist.*, row_number() over (partition by hist.pk order by hist.revision) as row_number
    from tms.tms_agent_stats_history hist
    where hist.start_time > date_trunc ('day', CURRENT_TIMESTAMP)
    order by hist.pk, hist.revision
), state_history as (
    select hist1.extension, hist1.revision,
           hist1.previous_state as previous_state,
           hist1.state_start_time as state_start_time, 
           hist2.state_start_time as state_end_time
    from stat_history hist1 inner join stat_history hist2 on hist1.pk = hist2.pk and hist1.row_number + 1 = hist2.row_number
    where hist1.state = 'WRAP'
), state_call_history_union as (
    select 1 as num, 
        shist.extension as extension, 
        shist.revision as revision, 
        shist.state_start_time as state_start_time, 
        shist.state_end_time as state_end_time
    from state_history shist
    where shist.previous_state = 'ONCALL'
union
    select 2 as num,
        chist.agent_extension as extension, 
        max(chist.revision) as revision,
        null as state_start_time,
        null as state_end_time
    from tms.tms_agent_call_history chist
    where chist.revision_type <> 2
    and chist.call_direction <> 'INTERNAL'
    and chist.revision > (select min(stat.revision) from stat_history stat)
    group by chist.agent_extension, chist.pk
), state_call_history as (
    select hist.*, row_number() over (partition by hist.extension order by hist.revision) as row_number
    from state_call_history_union hist
    order by hist.revision
), last_calls as (
    select hist1.extension, hist1.revision, hist2.state_start_time, hist2.state_end_time
    from state_call_history hist1 inner join state_call_history hist2 on hist1.extension = hist2.extension and hist1.row_number + 1 = hist2.row_number
    where hist1.num = 2 and hist2.num = 1
)
select usr.last_name || ', ' || usr.first_name as agentName,
       last_call.state_start_time as wrapStartTime,
       last_call.state_end_time as wrapEndTime,
       queue.queue_name as queueName,
       code.disposition as disposition
from last_calls last_call
inner join ams.ams_user usr on usr.extension = last_call.extension
left join tms.tms_agent_call_history call_history on call_history.revision = last_call.revision
left join svc.sv_dialer_queue queue on call_history.queue_pk = queue.pk
left join svc.sv_call_disposition_code code on call_history.disposition_id = code.disposition_id