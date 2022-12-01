package com.bluechemi.application.viewDevices


enum class DevicesStateEnum {
    PLAYING {
        override fun toString(): String{
            return "ic_play"
        }
    },
    STOPPED {
        override fun toString(): String{
            return "ic_pause"
        }
    }
}

enum class Connection{
    CONNECTED{
        override fun toString(): String {
            return "연결 됨"
        }
    },
    DISCONNECTED{
        override fun toString(): String {
            return "연결 안 됨"
        }
    }
}