with disposition_summary as (
    select 
        stats.pk as pk,
        count(nullif(code.contact, false)) as contact_count,
        count(nullif(code.disposition_id in (20), false)) as ptp_count
    from tms.tms_dialer_stats stats
        inner join tms.tms_dialer_queue queue on queue.queue_stats_pk = stats.pk
        left join tms.tms_dialer_call call on call.stats_pk = stats.pk
        left join svc.sv_call_disposition_code code on call.disposition_code_id = code.disposition_id
    where queue.pk = :queuePk
    group by stats.pk
)
select 
    queue.pk as queue_pk,
    queue.queue_name as queue_name,
    stats.pk as run_id,
    stats.state as state,
    stats.start_time as start_time,
    stats.end_time as end_time,
    stats.total_loan_count as total_loan_count,
    stats.in_progress_loan_count as in_progress_loan_count,
    stats.ready_loan_count + stats.not_ready_loan_count as remaining_loan_count,
    stats.in_progress_call_count + stats.pending_call_count as in_progress_call_count,
    stats.scheduled_call_count as scheduled_call_count,
    stats.dropped_call_count + stats.rejected_call_count + stats.successful_call_count + stats.failed_call_count as calls_made,
    disp.contact_count,
    disp.ptp_count
from tms.tms_dialer_stats stats 
    inner join disposition_summary disp on stats.pk = disp.pk
    inner join svc.sv_dialer_queue queue on queue.pk = stats.queue_pk
    