with day_stats as (
    select *
    from tms.tms_agent_stats stats 
    where stats.start_time > date_trunc('day', CURRENT_TIMESTAMP)
), stat_summary as (
    select
        stats.extension as extension,
        min(stats.start_time) as start_time,
        max(coalesce(stats.end_time, current_timestamp)) as end_time,
        sum(cast(substring(stats.total_idle_time from 'PT(.*)S') as float)) as total_idle_time,
        sum(cast(substring(stats.total_on_call_time from 'PT(.*)S') as float)) as total_on_call_time,
        sum(cast(substring(stats.total_hold_time from 'PT(.*)S') as float)) as total_hold_time,
        sum(cast(substring(stats.total_wrap_time from 'PT(.*)S') as float)) as total_wrap_time,
        sum(cast(substring(stats.total_preview_time from 'PT(.*)S') as float)) as total_preview_time,
        sum(cast(substring(stats.total_break_time from 'PT(.*)S') as float)) as total_break_time,
        sum(cast(substring(stats.total_meeting_time from 'PT(.*)S') as float)) as total_meeting_time,
        sum(cast(substring(stats.total_ready_time from 'PT(.*)S') as float)) as total_ready_time,
        sum(cast(substring(stats.utilization_break_time from 'PT(.*)S') as float)) as utilization_break_time,
        sum(cast(substring(stats.utilization_hold_time from 'PT(.*)S') as float)) as utilization_hold_time,
        sum(cast(substring(stats.utilization_idle_time from 'PT(.*)S') as float)) as utilization_idle_time,
        sum(cast(substring(stats.utilization_meeting_time from 'PT(.*)S') as float)) as utilization_meeting_time,
        sum(cast(substring(stats.utilization_preview_time from 'PT(.*)S') as float)) as utilization_preview_time,
        sum(cast(substring(stats.utilization_wrap_time from 'PT(.*)S') as float)) as utilization_wrap_time
    from day_stats stats
    group by stats.extension
), stat_calls as (
    select distinct
        stats.extension as extension,
         call.call_direction as direction, 
         call.call_uuid as call_uuid, 
         call.auto_dialed as auto_dialed
    from day_stats stats 
        inner join tms.tms_agent_call_history call on call.stats_pk = stats.pk
    where call.revision_type <> 2
    and call.call_direction <> 'INTERNAL'
), call_summary as (
    select calls.extension,
        count(nullif(calls.direction = 'INBOUND', false)) as inboundCallCount,
        count(nullif(calls.direction = 'OUTBOUND' and calls.auto_dialed = true, false)) as dialerCallCount,
        count(nullif(calls.direction = 'OUTBOUND' and calls.auto_dialed = true and code.contact = true, false)) as dialerContactCount,
        count(nullif(calls.direction = 'OUTBOUND' and calls.auto_dialed = false, false)) as manualCallCount,
        count(nullif(calls.direction = 'OUTBOUND' and calls.auto_dialed = false and code.contact = true, false)) as manualContactCount,
        count(nullif(code.contact, false)) as contactCount,
        count(nullif(code.success, false)) as success,
        count(nullif(code.contact and calls.direction = 'OUTBOUND' and calls.auto_dialed = true, false)) as dialersuccess,
        count(nullif(code.contact and calls.direction = 'OUTBOUND' and calls.auto_dialed = false, false)) as manualsuccess,
        count(nullif(code.disposition_id in (20) and calls.direction = 'OUTBOUND' and calls.auto_dialed = true, false)) as DialerptpCount,
        count(nullif(code.disposition_id in (20) and calls.direction = 'OUTBOUND' and calls.auto_dialed = false, false)) as ManualptpCount,
        count(nullif(code.disposition_id in (20), false)) as ptpCount
    from stat_calls calls
        left join tms.tms_call_detail_record cdr on cdr.call_uuid = calls.call_uuid
        left join svc.sv_call_disposition_code code on cdr.user_dispostion_code = code.disposition_id
    group by calls.extension
)
select 
    usr.last_name || ', ' || usr.first_name as agent_name,
    stat_sum.start_time,
    stat_sum.end_time,
    stat_sum.total_idle_time,
    stat_sum.total_on_call_time,
    stat_sum.total_hold_time,
    stat_sum.total_wrap_time,
    stat_sum.total_preview_time,
    stat_sum.total_break_time,
    stat_sum.total_meeting_time,
    stat_sum.total_ready_time,
    stat_sum.utilization_break_time,
    stat_sum.utilization_hold_time,
    stat_sum.utilization_idle_time,
    stat_sum.utilization_meeting_time,
    stat_sum.utilization_preview_time,
    stat_sum.utilization_wrap_time,
    call_sum.inboundCallCount,
    call_sum.dialerCallCount,
    call_sum.dialerContactCount,
    call_sum.manualCallCount,
    call_sum.manualContactCount,
    call_sum.contactCount,
    call_sum.dialersuccess,
    call_sum.manualsuccess,
    call_sum.dialerptpcount,
    call_sum.manualptpcount,
    call_sum.ptpcount,
    call_sum.success

from call_summary call_sum inner join stat_summary stat_sum on call_sum.extension = stat_sum.extension
    inner join ams.ams_user usr on usr.extension = stat_sum.extension