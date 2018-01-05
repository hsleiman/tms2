uuid = session:getVariable("uuid");
api = freeswitch.API();
sc = "sched_api @15 "..uuid.." uuid_displace "..uuid.." start tone_stream://v=-30;%(100,100,800) 0 mux";
freeswitch.console_log("info",sc);
reply = api:executeString(sc);
session:setVariable("session_in_hangup_hook", "TRUE" );
session:setVariable("api_hangup_hook", "sched_del "..uuid );