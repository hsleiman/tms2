with agent_call_summary as (
    select 
        agent_call.agent_extension as extension,
        count(*) as call_count,
        count(nullif(code.contact, false)) as contact_count,
        count(nullif(code.disposition_id in (20), false)) as ptp_count
    from tms.tms_dialer_call dialer_call
        inner join tms.tms_agent_call_history agent_call on agent_call.call_uuid = dialer_call.call_uuid
        left join svc.sv_call_disposition_code code on agent_call.disposition_id = code.disposition_id
    where dialer_call.stats_pk = :statsPk
    and dialer_call.queue_pk = :queuePk
    and agent_call.call_state = 'DONE'
    group by agent_call.agent_extension
)
select 
    usr.last_name || ', ' || usr.first_name as agent_name,
    summary.call_count,
    summary.contact_count,
    summary.ptp_count
from agent_call_summary summary
    inner join ams.ams_user usr on usr.extension = summary.extension