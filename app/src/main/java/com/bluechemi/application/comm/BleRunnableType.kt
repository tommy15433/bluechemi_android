package com.bluechemi.application.comm

class BleRunnableType(
    val dev: BleDevice,
    val op: operation,
    val char: String?,
    val data: Int?
        ): Comparable<BleRunnableType>{
    override fun compareTo(other: BleRunnableType): Int {
        return compareValuesBy(this, other,
            { it.dev.uid },
            { it.op },
            { it.char },
            { it.data },
        )
    }

    override fun toString(): String {
        return "${dev.uid}, ${op.toString()}, ${char.toString()}, ${data.toString()}"
    }

}
