with call_stats as (
    select call.stats_pk as stats_pk,
        count(*) as dialCount,
        count(nullif(code.sip_code = 486, false)) as busyCount,
        count(nullif(coalesce(code.sip_code,0) <> 486 and ((cdr.amd_status is not null and cdr.dialplan_type in ('CallOutWithAMD', 'ConnectCallToAgent')) or cdr.dialplan_type = 'PowerDialer'), false)) as answerCount,
        count(nullif(coalesce(code.sip_code,0) <> 486 and (cdr.amd_status is null and cdr.dialplan_type in ('CallOutWithAMD', 'ConnectCallToAgent')) , false)) as noAnswerCount,
        count(nullif(cdr.answered is not null and cdr.answered, false)) as connectCount,
        count(nullif(cdr.amd_status = 'machine', false)) as detectedAsMachine,
        count(nullif(call.state = 'DROPPED' and cdr.amd_status = 'person', false)) as abandonedCount,
        count(nullif(code.contact, false)) as contactCount,
        count(nullif(code.disposition_id in (20), false)) as ptpCount,
        count(nullif(cdr.dialplan_type = 'PowerDialer', false)) as preview
    from tms.tms_dialer_queue queue 
        inner join tms.tms_dialer_call call on queue.queue_stats_pk = call.stats_pk
        left join tms.tms_call_detail_record cdr on cdr.call_uuid = call.call_uuid
        left join svc.sv_call_disposition_code code on call.disposition_code_id = code.disposition_id
    where queue.pk = :queuePk
    group by call.stats_pk
)
select 
    queue.queue_name as queueName,
    stats.start_time as startTime,
    stats.end_time as endTime,
    stats.total_loan_count as loans,
    callstats.dialCount as dials,
    callstats.busyCount as busy,
    callstats.answerCount as answered,
    callstats.noAnswerCount as sitna,
    callstats.connectCount as connects,
    callstats.abandonedCount as abandonded,
    callstats.contactCount as contacts,
    callstats.ptpCount as ptps
from svc.sv_dialer_queue queue 
    inner join tms.tms_dialer_stats stats on queue.pk = stats.queue_pk
    inner join call_stats callstats on callstats.stats_pk = stats.pk