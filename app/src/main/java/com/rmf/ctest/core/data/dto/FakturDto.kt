package com.rmf.ctest.core.data.dto

data class FakturDto(
    val nomor_pelanggan: String,
    val nama_pelanggan: String,
    val nama_lembaga: String,
    val nomor_faktur: String,
    val tanggal_diterima: String,
    val sistem_pembayaran: String,
    val total_modal: Long,
    val total_faktur: Long,
    val keterangan: String
)
