package com.ivaneye.naivechain

import java.io.Serializable


class Message : Serializable {
    var type: Int = 0
    var data: String? = null

    constructor() {}

    constructor(type: Int) {
        this.type = type
    }

    constructor(type: Int, data: String) {
        this.type = type
        this.data = data
    }
}