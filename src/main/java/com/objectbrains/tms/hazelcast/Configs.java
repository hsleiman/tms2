/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast;

import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.Member;
import com.hazelcast.mapreduce.aggregation.impl.SetAdapter;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.hcms.hazelcast.HazelcastService.AtomicReferenceKey;
import com.objectbrains.hcms.hazelcast.HazelcastService.ExecutorServiceKey;
import com.objectbrains.hcms.hazelcast.HazelcastService.MapKey;
import com.objectbrains.hcms.hazelcast.HazelcastService.MultiMapKey;
import com.objectbrains.hcms.hazelcast.HazelcastService.QueueKey;
import com.objectbrains.hcms.hazelcast.HazelcastService.ReplicatedMapKey;
import com.objectbrains.sti.pojo.DialerQueueAccountDetails;
import com.objectbrains.sti.pojo.DialerQueueRecord;
import com.objectbrains.tms.db.entity.DNC;
import com.objectbrains.tms.db.entity.cdr.CallDetailRecord;
import com.objectbrains.tms.db.entity.freeswitch.FreeswitchNode;
import com.objectbrains.tms.db.entity.freeswitch.StaticDialplan;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.hazelcast.entity.AgentStats;
import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.hazelcast.entity.DialerCall;
import com.objectbrains.tms.hazelcast.entity.DialerLoan;
import com.objectbrains.tms.hazelcast.entity.DialerStats;
import com.objectbrains.tms.hazelcast.entity.PrimaryCall;
import com.objectbrains.tms.hazelcast.entity.WaitingCall;
import com.objectbrains.tms.hazelcast.keys.AgentQueueKey;
import com.objectbrains.tms.hazelcast.keys.StaticDialplanKey;
import com.objectbrains.tms.hazelcast.keys.TMSDialplanKey;
import com.objectbrains.tms.pojo.UploadCallRecordingPOJO;
import com.objectbrains.tms.service.ReportService;
import com.objectbrains.tms.service.dialer.Dialer;
import com.objectbrains.tms.service.dialer.LoanNumber;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 *
 * @author connorpetty
 */
@Configuration
public class Configs implements BeanFactoryAware {

    public static final String AGENT_MAP_STORE_BEAN_NAME = "agentMapStore";
    public static final String AGENT_QUEUE_ASSOCIATION_MAP_STORE_BEAN_NAME = "agentQueueAssociationMapLoader";
    public static final String DNC_MAP_STORE_BEAN_NAME = "dncMapStore";

    public static final String TMS_DIALPLAN_MAP_STORE_BEAN_NAME = "tmsDialplanMapStore";

    // public static final String TMS_DIALPLAN_KEY_MAP_STORE_BEAN_NAME = "tmsDialplanKeyMapStore";
    public static final String FREESWITCH_NODE_MAP_STORE_BEAN_NAME = "freeswitchNodeMapStore";

    public static final String CALL_DETAIL_RECORD_MAP_STORE_BEAN_NAME = "callDetailRecordMapStore";
    public static final String STATIC_DIALPLAN_MAP_STORE_BEAN_NAME = "staticDialplanMapStore";
    public static final String AGENT_STATS_MAP_STORE_BEAN_NAME = "agentStatsMapStore";
    public static final String AGENT_CALL_MAP_STORE_BEAN_NAME = "agentCallMapStore";
//    public static final String DIALER_LOAN_MAP_STORE_BEAN_NAME = "dialerLoanMapStore";
    public static final String DIALER_LOAN_MAP_STORE_FACTORY_BEAN_NAME = "dialerLoanMapStoreFactory";
    public static final String DIALER_CALL_MAP_STORE_BEAN_NAME = "dialerCallMapStore";
    public static final String DIALER_STATS_MAP_STORE_BEAN_NAME = "dialerStatsMapStore";

    public static final AtomicReferenceKey<ReportService.TodaysReportCache> TODAYS_REPORT_CACHE_REF = new AtomicReferenceKey("todaysReports");
    public static final MapKey<Integer, Agent> AGENT_MAP = new MapKey<>("agents");
    public static final MapKey<String, DNC> DNC_MAP = new MapKey<>("dnc");
    public static final MapKey<Long, Dialer> DIALER_MAP = new MapKey<>("dialers");
    public static final MapKey<Long, Long> QUEUE_TO_DIALER_MAP = new MapKey<>("queueTodialers");

    public static final MapKey<TMSDialplanKey, TMSDialplan> TMSDIALPLAN_MAP = new MapKey<>("tmsDialplan");

    public static final MapKey<String, String> AGENT_TRANSFER_MAP = new MapKey<>("agent_transfer_map");
    public static final MapKey<String, String> TRANSFER_CALLUUID_TO_CALLUUID = new MapKey<>("TRANSFER_CALLUUID_TO_CALLUUID");
    public static final MapKey<String, Integer> TRANSFER_CALLUUID_TO_EXT = new MapKey<>("TRANSFER_CALLUUID_TO_EXT");

    public static final MapKey<String, Long> AGENT_TRANSFER_QUEUE_MAP = new MapKey<>("agent_transfer_queue_map");
    public static final MapKey<String, Integer> AGENT_TO_CALLUUID_TRANSFER_MAP_LOCK = new MapKey<>("agent_to_calluuid_transfer_map_lock");
    //public static final MapKey<String, TMSDialplanKey> TMSDIALPLANKEYS_MAP = new MapKey<>("tmsDialplanKey");

    public static final MapKey<String, FreeswitchNode> FREESWITCH_NODE_MAP = new MapKey<>("freeswitchNode");
    public static final MapKey<String, String> FREESWITCH_NODE_TO_CALL_MAP = new MapKey<>("freeswitchNodeToCall");
    public static final MapKey<Integer, String> FREESWITCH_NODE_TO_EXT_MAP = new MapKey<>("freeswitchNodeToExt");

    public static final MapKey<StaticDialplanKey, StaticDialplan> STATIC_DIALPLAN_MAP = new MapKey<>("staticDialplan");

    //public static final MultiMapKey<FreeswitchContext, FreeswitchNode> FREESWITCH_NODE_MULTI_MAP = new HazelcastService.MultiMapKey<>("freeswitchNode");
    public static final MapKey<AgentQueueKey, AgentWeightedPriority> QUEUE_WEIGHTED_PRIORITY_MAP = new MapKey<>("queueWeightedPriority");
    public static final MapKey<Long, DialerQueueRecord> DIALER_QUEUE_RECORD_MAP = new MapKey<>("dialerQueueRecords");
    public static final MapKey<String, DialerCall> DIALER_CALL_MAP = new MapKey<>("dialerCalls");
    public static final MapKey<Long, DialerStats> DIALER_STATS_MAP = new MapKey<>("dialerStats");
    public static final MapKey<String, CallDetailRecord> CALL_DETAIL_RECORD_MAP = new MapKey<>("callDetailRecord");

    public static final QueueKey<UploadCallRecordingPOJO> CALL_RECORDING_UPLOAD_QUEUE = new QueueKey<>("callRecordingUploadQueue");

    public static final MapKey<Integer, QueueAdapter<PrimaryCall>> AGENT_TO_PRIMARY_CALL_MAP = new MapKey<>("agentToPrimaryCall");
    public static final MapKey<Integer, AgentStats> AGENT_STATS_MAP = new MapKey<>("agentStats");

    public static final MapKey<String, Long> Call_TIMEOUT_ON_DIALER = new MapKey<>("callTimeoutOnDialer");
    public static final MapKey<String, Long> CDR_TIMEOUT_TO_SVC = new MapKey<>("cdrTimeoutToSVC");
    public static final MapKey<String, Boolean> IVR_AUTHORIZED_MAP = new MapKey<>("ivr_authorized_map");

    public static final MapKey<Integer, SetAdapter<AgentCall>> AGENT_CALL_MAP = new MapKey<>("agentCalls");
    public static final MapKey<Long, QueueAdapter<WaitingCall>> QUEUE_WAITING_CALL_MAP = new MapKey<>("waitingCalls");
    public static final ExecutorServiceKey DIALER_EXECUTOR_SERVICE = new ExecutorServiceKey("dialerExecutor");
    public static final ExecutorServiceKey WEBSOCKET_EXECUTOR_SERVICE = new ExecutorServiceKey("websocketExecutor");

    public static final ExecutorServiceKey DIALPLAN_EXECUTOR_SERVICE = new ExecutorServiceKey("dialpanExecutor");
    public static final ExecutorServiceKey AGENT_CALL_EXECUTOR_SERVICE = new ExecutorServiceKey("agentCallCleanupExecutor");

    public static final MultiMapKey<Integer, Integer> WEBSOCKET_BI_MONITOR_MAP = new MultiMapKey<Integer, Integer>("websocketBiMap");
    public static final ReplicatedMapKey<Integer, Member> WEBSOCKET_AGENT_ROUTING_MAP = new ReplicatedMapKey<>("websocketRoutingTable");

    private static final String DIALER_LOANS_MAP_PREFIX = "dialerLoans:";
    private static final String DIALER_LOAN_DETAILS_MAP_PREFIX = "dialerLoanDetails:";

    public static IQueue<DialerQueueAccountDetails> getNotReadyLoansQueue(HazelcastService hazelcastService, long dialerPk) {
        return hazelcastService.getQueue("notReadyLoans: " + dialerPk);
    }

    public static IQueue<DialerQueueAccountDetails> getReadyLoansQueue(HazelcastService hazelcastService, long dialerPk) {
        return hazelcastService.getQueue("readyLoans:" + dialerPk);
    }

    public static IQueue<LoanNumber> getRetryCallQueue(HazelcastService hazelcastService, long dialerPk) {
        return hazelcastService.getQueue("retryCalls:" + dialerPk);
    }

    public static IMap<Long, DialerLoan> getDialerLoanMap(HazelcastService hazelcastService, long dialerPk) {
        return hazelcastService.getMap(DIALER_LOANS_MAP_PREFIX + dialerPk);
    }

    public static IMap<Long, DialerQueueAccountDetails> getDialerLoanDetailsMap(HazelcastService hazelcastService, long dialerPk) {
        return hazelcastService.getMap(DIALER_LOAN_DETAILS_MAP_PREFIX + dialerPk);
    }

    public static ILock getDialerLock(HazelcastService hazelcastService, long dialerPk) {
        return hazelcastService.getLock("dialerLock:" + dialerPk);
    }

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    //converts hours to seconds
    private static int hours(int value) {
        return (int) TimeUnit.SECONDS.convert(value, TimeUnit.HOURS);
    }

    //converts days to seconds
    private static int days(int value) {
        return (int) TimeUnit.SECONDS.convert(value, TimeUnit.DAYS);
    }

    //converts minutes to seconds
    private static int minutes(int value) {
        return (int) TimeUnit.SECONDS.convert(value, TimeUnit.MINUTES);
    }

    private MapConfig buildMapConfig(MapKey mapKey, String mapStoreBeanName, boolean useNearCache) {
        return buildMapConfig(mapKey, InitialLoadMode.LAZY, mapStoreBeanName, useNearCache);
    }

    private MapConfig buildMapConfig(MapKey mapKey, InitialLoadMode loadMode, String mapStoreBeanName, boolean useNearCache) {
        MapConfig config = new MapConfig(mapKey.getName());
        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setInitialLoadMode(loadMode);
        mapStoreConfig.setImplementation(beanFactory.getBean(mapStoreBeanName));
        config.setMapStoreConfig(mapStoreConfig);
        if (useNearCache) {
            config.setNearCacheConfig(new NearCacheConfig());
        }
        return config;
    }

//    private QueueConfig buildQueueConfig(QueueKey queueKey, long bulkLoad, String queueStoreBeanName) {
//        QueueConfig config = new QueueConfig(queueKey.getName());
//        QueueStoreConfig queueStoreConfig = new QueueStoreConfig();
//        queueStoreConfig.setProperty("bulk-load", bulkLoad + "");
//        queueStoreConfig.setStoreImplementation(beanFactory.getBean(queueStoreBeanName, FreeswitchNodeQueueStore.class));
//        config.setQueueStoreConfig(queueStoreConfig);
//
//        return config;
//    }
    //CDR_TIMEOUT_TO_SVC
    @Bean
    public MapConfig cdrTimeoutToSVCMapConfig() {
        MapConfig config = new MapConfig(CDR_TIMEOUT_TO_SVC.getName());
        config.setTimeToLiveSeconds(hours(1));//60 minutes
        return config;
    }

    @Bean
    public MapConfig ivrAuthorizedMapConfig() {
        MapConfig config = new MapConfig(IVR_AUTHORIZED_MAP.getName());
        config.setTimeToLiveSeconds(hours(1));//60 minutes
        return config;
    }

    @Bean
    public MapConfig callTimeoutOnDialerMapConfig() {
        MapConfig config = new MapConfig(Call_TIMEOUT_ON_DIALER.getName());
        config.setTimeToLiveSeconds(hours(1));//60 minutes
        return config;
    }

    @Bean
    @DependsOn(AGENT_MAP_STORE_BEAN_NAME)
    public MapConfig agentMapConfig() {
        MapConfig config = buildMapConfig(AGENT_MAP,
                InitialLoadMode.EAGER, AGENT_MAP_STORE_BEAN_NAME, true);
        return config;
    }

    @Bean
    @DependsOn(AGENT_QUEUE_ASSOCIATION_MAP_STORE_BEAN_NAME)
    public MapConfig agentQueueMapConfig() {
        MapConfig config = buildMapConfig(QUEUE_WEIGHTED_PRIORITY_MAP,
                InitialLoadMode.EAGER, AGENT_QUEUE_ASSOCIATION_MAP_STORE_BEAN_NAME, true);
        return config;
    }

    @Bean
    @DependsOn(DNC_MAP_STORE_BEAN_NAME)
    public MapConfig dncMapConfig() {
        MapConfig config = buildMapConfig(DNC_MAP,
                DNC_MAP_STORE_BEAN_NAME, true);
        return config;
    }

    @Bean
    @DependsOn(TMS_DIALPLAN_MAP_STORE_BEAN_NAME)
    public MapConfig tmsDialplanMapConfig() {
        MapConfig config = buildMapConfig(TMSDIALPLAN_MAP,
                InitialLoadMode.EAGER, TMS_DIALPLAN_MAP_STORE_BEAN_NAME, false);
        config.setMaxIdleSeconds(minutes(15));
        return config;
    }

    @Bean
    public MapConfig agentTransferMapConfig() {
        MapConfig config = new MapConfig(AGENT_TRANSFER_MAP.getName());
        config.setMaxIdleSeconds(minutes(1));
        return config;
    }

    @Bean
    public MapConfig transferCallUUIDToCallUUIDrMapConfig() {
        MapConfig config = new MapConfig(TRANSFER_CALLUUID_TO_CALLUUID.getName());
        config.setMaxIdleSeconds(hours(1));
        return config;
    }

    @Bean
    public MapConfig transferCallUUIDToEXTrMapConfig() {
        MapConfig config = new MapConfig(TRANSFER_CALLUUID_TO_EXT.getName());
        config.setMaxIdleSeconds(hours(1));
        return config;
    }

    @Bean
    public MapConfig agentTransferQueueMapConfig() {
        MapConfig config = new MapConfig(AGENT_TRANSFER_QUEUE_MAP.getName());
        config.setMaxIdleSeconds(hours(1));
        return config;
    }

    @Bean
    public MapConfig agentToCallUUIDTransferQueueMapLockConfig() {
        MapConfig config = new MapConfig(AGENT_TO_CALLUUID_TRANSFER_MAP_LOCK.getName());
        config.setMaxIdleSeconds(minutes(2));
        return config;
    }

    @Bean
    @DependsOn(FREESWITCH_NODE_MAP_STORE_BEAN_NAME)
    public MapConfig freeswitchNodeMapConfig() {
        MapConfig config = buildMapConfig(FREESWITCH_NODE_MAP,
                FREESWITCH_NODE_MAP_STORE_BEAN_NAME, false);
        config.setMaxIdleSeconds(hours(1));
        return config;
    }

    @Bean
    public MapConfig freeswitchNodToCallConfig() {
        MapConfig config = new MapConfig(FREESWITCH_NODE_TO_CALL_MAP.getName());
        config.setTimeToLiveSeconds(hours(2));//120 minutes
        return config;
    }

    @Bean
    public MapConfig freeswitchNodToExtConfig() {
        MapConfig config = new MapConfig(FREESWITCH_NODE_TO_EXT_MAP.getName());
        config.setTimeToLiveSeconds(hours(8));//8 Hours
        return config;
    }

//    @Bean
//    @DependsOn(TMS_DIALPLAN_KEY_MAP_STORE_BEAN_NAME)
//    public MapConfig tmsDialplanKeyMapConfig() {
//        MapConfig config = buildMapConfig(TMSDIALPLANKEYS_MAP,
//                InitialLoadMode.EAGER, TMS_DIALPLAN_KEY_MAP_STORE_BEAN_NAME, false);
//        config.setMaxIdleSeconds(15 * 60);
//        return config;
//    }
//    @Bean
//    @DependsOn(FREESWITCH_NODE_QUEUE_STORE_BEAN_NAME)
//    public QueueConfig freeswitchNodeQueueConfig() {
//        QueueConfig config = buildQueueConfig(FREESWITCH_NODE_QUEUE, 500l, FREESWITCH_NODE_QUEUE_STORE_BEAN_NAME);
//        return config;
//    }
    @Bean
    @DependsOn(CALL_DETAIL_RECORD_MAP_STORE_BEAN_NAME)
    public MapConfig callDetailRecordMapConfig() {
        MapConfig config = buildMapConfig(CALL_DETAIL_RECORD_MAP,
                InitialLoadMode.EAGER, CALL_DETAIL_RECORD_MAP_STORE_BEAN_NAME, false);
        config.setMaxIdleSeconds(minutes(15));
        return config;
    }

    @Bean
    @DependsOn(STATIC_DIALPLAN_MAP_STORE_BEAN_NAME)
    public MapConfig staticDialplanMapConfig() {
        MapConfig config = buildMapConfig(STATIC_DIALPLAN_MAP,
                InitialLoadMode.EAGER, STATIC_DIALPLAN_MAP_STORE_BEAN_NAME, true);
        config.setMaxIdleSeconds(10);
        config.setEvictionPolicy(EvictionPolicy.LRU);
        return config;
    }

    @Bean
    public MapConfig agentToPrimaryCallMapConfig() {
        MapConfig config = new MapConfig(AGENT_TO_PRIMARY_CALL_MAP.getName());
        config.setTimeToLiveSeconds(minutes(30));//30 minutes
        return config;
    }

    @Bean
    @DependsOn(AGENT_STATS_MAP_STORE_BEAN_NAME)
    public MapConfig agentStatsMapConfig() {
        MapConfig config = buildMapConfig(AGENT_STATS_MAP,
                InitialLoadMode.EAGER, AGENT_STATS_MAP_STORE_BEAN_NAME, false);
        config.setMaxIdleSeconds(hours(1));//1 hour
        return config;
    }

    @Bean
    @DependsOn(AGENT_CALL_MAP_STORE_BEAN_NAME)
    public MapConfig agentCallsMapConfig() {
        MapConfig config = buildMapConfig(AGENT_CALL_MAP,
                InitialLoadMode.EAGER, AGENT_CALL_MAP_STORE_BEAN_NAME, false);
        config.setMaxIdleSeconds(minutes(10));//10 min
        return config;
    }

//    @Bean
//    public MapConfig queueToDialerMapConfig() {
//        MapConfig config = new MapConfig(QUEUE_TO_DIALER_MAP.getName());
//        NearCacheConfig nearCacheConfig = new NearCacheConfig();
////        nearCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
//        nearCacheConfig.setCacheLocalEntries(true);
//        config.setNearCacheConfig(nearCacheConfig);
//        config.setBackupCount(2);
//        return config;
//    }

    @Bean
    public MapConfig dialerMapConfig() {
        MapConfig config = new MapConfig(DIALER_MAP.getName());
        NearCacheConfig nearCacheConfig = new NearCacheConfig();
        nearCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        nearCacheConfig.setCacheLocalEntries(true);
        config.setNearCacheConfig(nearCacheConfig);
        config.setTimeToLiveSeconds(days(1));
        config.setBackupCount(2);
        return config;
    }

    @Bean
    @DependsOn(DIALER_LOAN_MAP_STORE_FACTORY_BEAN_NAME)
    public MapConfig dialerLoanMapConfig() {
        MapConfig config = new MapConfig(DIALER_LOANS_MAP_PREFIX + "*");
        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setInitialLoadMode(InitialLoadMode.EAGER);
        mapStoreConfig.setFactoryImplementation(beanFactory.getBean(DIALER_LOAN_MAP_STORE_FACTORY_BEAN_NAME));
        config.setMapStoreConfig(mapStoreConfig);
        return config;
    }

    @Bean
    public MapConfig dialerLoanDetailsMapConfig() {
        MapConfig config = new MapConfig(DIALER_LOAN_DETAILS_MAP_PREFIX + "*");
        NearCacheConfig nearCacheConfig = new NearCacheConfig();
//        nearCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        nearCacheConfig.setCacheLocalEntries(true);
        config.setNearCacheConfig(nearCacheConfig);
        config.setBackupCount(2);
        config.setReadBackupData(true);
        return config;
    }

    @Bean
    @DependsOn(DIALER_CALL_MAP_STORE_BEAN_NAME)
    public MapConfig dialerCallsMapConfig() {
        return buildMapConfig(DIALER_CALL_MAP,
                InitialLoadMode.EAGER, DIALER_CALL_MAP_STORE_BEAN_NAME, false);
    }

    @Bean
    @DependsOn(DIALER_STATS_MAP_STORE_BEAN_NAME)
    public MapConfig dialerStatsMapConfig() {
        return buildMapConfig(DIALER_STATS_MAP,
                InitialLoadMode.EAGER, DIALER_STATS_MAP_STORE_BEAN_NAME, false);
    }

    @Bean
    public ExecutorConfig dialerExecutorConfig() {
        ExecutorConfig config = new ExecutorConfig(DIALER_EXECUTOR_SERVICE.getName(), 64);
        config.setStatisticsEnabled(false);
        return config;
    }

    @Bean
    public ExecutorConfig dialpanExecutorConfig() {
        ExecutorConfig config = new ExecutorConfig(DIALPLAN_EXECUTOR_SERVICE.getName(), 25);
        config.setStatisticsEnabled(false);
        return config;
    }

    @Bean
    public ExecutorConfig websocketExecutorConfig() {
        ExecutorConfig config = new ExecutorConfig(WEBSOCKET_EXECUTOR_SERVICE.getName(), 25);
        config.setStatisticsEnabled(false);
        return config;
    }

    @Bean
    public ExecutorConfig agentCallExecutorConfig() {
        ExecutorConfig config = new ExecutorConfig(AGENT_CALL_EXECUTOR_SERVICE.getName(), Runtime.getRuntime().availableProcessors());
        config.setStatisticsEnabled(false);
        return config;
    }
}
