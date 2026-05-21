package com.odtheking.odin.features.impl.dungeon.autoroutes

enum class RouteState {
    IDLE,           // no active route
    WAITING,        // on starting point, about to begin
    EXECUTING,      // mid-step
    COMPLETE,       // all steps done
    FAILED          // something went wrong
}