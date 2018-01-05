/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

def name='hsleiman'

println "Hello $name!"

action answer()
action sleep(500)
action playback("/usr/local/freeswitch/sounds/TMS-Sound/ivr/ava/8000/phrase/WELCOME_TO_CASHCALL.wav") 
action sleep(500)
action playback(toneStream(500,500,300,700))
action hangup("NORMAL_CLEARING")



