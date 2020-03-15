package org.diacalc.android.maths

class User {
    var login: String
    var pass: String
    var server: String
    private var fcs: Factors
    var round: Int
    var isMmol: Boolean
    var isPlasma: Boolean
    var s1: Float
    var s2 : Float  //sugars to restore after timer
    var isTimeSense: Boolean
    var targetSugar: Float //sugars target
    var lowSugar: Float //sugars low level
    var hiSugar: Float //sugars hi level
    var menuInfo: Int

    constructor(login: String, pass: String, server: String, fcs: Factors,
                round: Int, plasma: Boolean, mmol: Boolean,
                s1: Float, s2: Float, timeSense: Boolean, target: Float,
                low: Float, hi: Float, menuInfo: Int) {
        this.login = login
        this.pass = pass
        this.server = server
        this.fcs = fcs
        this.round = round
        isPlasma = plasma
        isMmol = mmol
        this.s1 = s1
        this.s2 = s2
        isTimeSense = timeSense
        targetSugar = target
        lowSugar = low
        hiSugar = hi
        this.menuInfo = menuInfo
    }


    constructor(user: User)  {
        login = user.login
        pass = user.pass
        server = user.server
        fcs = Factors(user.fcs)
        round = user.round
        isPlasma = user.isPlasma
        isMmol = user.isMmol
        s1 = user.s1
        s2 = user.s2
        isTimeSense = user.isTimeSense
        targetSugar = user.targetSugar
        lowSugar = user.lowSugar
        hiSugar = user.hiSugar
        menuInfo = user.menuInfo
    }

    var factors: Factors
        get() = fcs
        set(value) {
            fcs = value
        }

    companion object {
        const val ROUND_1 = 1
        const val ROUND_05 = 2
        const val PFC_INFO = 0
        const val BE_INFO = 1
        const val CALOR_INFO = 2
        const val DOSE_INFO = 3
        const val DEF_SERVER = "http://diacalc.org/dbwork/"
        const val PLASMA = true
        const val WHOLE = false
        const val MMOL = true
        const val MGDL = false
        const val TIMESENSE = true
        const val NO_TIMESENSE = false
    }
}