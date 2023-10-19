package com.skripsi.estock.datasource.model

class DetailCompany(
    var id:String? = null,
    var name:String? = null,
    var code:String? = null,
    var gpm: gpm = gpm(),
    var npm:npm = npm(),
    var roe:roe = roe(),
    var der:der = der(),
){
    var nilaiPreferensi: Double = 0.0
    var roeSpkNormalisasi: Double = 0.0
    var npmSpkNormalisasi: Double = 0.0
    var gpmSpkNormalisasi: Double = 0.0
    var derSpkNormalisasi: Double = 0.0
    var c1xW: Double = 0.0
    var c2xW: Double = 0.0
    var c3xW: Double = 0.0
    var c4xW: Double = 0.0
}

class gpm(
    var gpm_stock: String? = null,
    var gpm_spk: String? = null

)

class npm(
    var npm_stock: String? = null,
    var npm_spk: String? = null
)

class roe(
    var roe_stock: String? = null,
    var roe_spk: String? = null
)

class der(
    var der_stock: String? = null,
    var der_spk: String? = null
)

