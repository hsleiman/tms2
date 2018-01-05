with stat_history as (
    select hist.*, row_number() over (partition by hist.pk order by hist.revision) as row_number
    from tms.tms_agent_stats_history hist
    order by hist.pk, hist.revision
), state_history as (
    select startState.extension as extension,
           startState.state as state,
           startState.state_start_time as start_time, 
           endState.state_start_time as end_time
    from stat_history startState inner join stat_history endState on startState.pk = endState.pk and startState.row_number + 1 = endState.row_number
    where startState.state in ('BREAK', 'LUNCH')
)
select usr.last_name || ', ' || usr.first_name as agentName,
        stats.start_time as startTime,
        stats.end_time as endTime,
        null as dialedNum,
        'Sign in/Sign out' as service,
        'End of Shift' as description
from tms.tms_agent_stats stats 
inner join ams.ams_user usr on usr.extension = stats.extension

union

select usr.last_name || ', ' || usr.first_name as agentName,
       states.start_time as startTime,
       states.end_time as endTome,
       null as dialedNum,
       'Sign in/Sign out' as service,
       case states.state
            when 'BREAK' then '15 minute Break'
            when 'LUNCH' then '60 minute Lunch'
       end as description
from state_history states
inner join ams.ams_user usr on usr.extension = states.extension

union

select usr.last_name || ', ' || usr.first_name as agentName,
     to_timestamp(min(info.revtstmp) / 1000) as startTime,
     to_timestamp(max(info.revtstmp) / 1000) as endTime,
     call_history.borrower_phone_number as dialedNum,
     queue.queue_name as service,
     call_history.call_direction as description
from tms.tms_agent_call_history call_history inner join tms.tms_revinfo info on call_history.revision = info.rev
inner join ams.ams_user usr on usr.extension = call_history.agent_extension
left join svc.sv_dialer_queue queue on call_history.queue_pk = queue.pk
where call_history.call_direction <> 'INTERNAL'
and (call_history.caller_hangup = false or call_history.caller_hangup_mod = true)
and (call_history.agent_hangup = false or call_history.agent_hangup_mod = true)
group by usr.last_name, usr.first_name, queue.queue_name, call_history.pk, call_history.borrower_phone_number, call_history.call_direction