package com.finbaby.app.sms

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Step 1: SMS Reader (ContentProvider)
 * Reads SMS messages from the device inbox and filters for financial messages.
 */
@Singleton
class SmsReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class RawSms(
        val id: String,
        val sender: String,
        val body: String,
        val date: Long
    )

    /**
     * Read financial SMS from inbox.
     * @param sinceTimestamp Only read SMS after this timestamp (epoch millis)
     * @param limit Max number of SMS to read
     */
    fun readFinancialSms(sinceTimestamp: Long = 0, limit: Int = 200): List<RawSms> {
        val smsList = mutableListOf<RawSms>()

        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )
        val selection = "${Telephony.Sms.DATE} > ?"
        val selectionArgs = arrayOf(sinceTimestamp.toString())
        val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT $limit"

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri, projection, selection, selectionArgs, sortOrder
            )

            cursor?.let {
                val idIdx = it.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIdx = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val sender = it.getString(addressIdx) ?: continue
                    val body = it.getString(bodyIdx) ?: continue
                    val id = it.getString(idIdx) ?: continue
                    val date = it.getLong(dateIdx)

                    // Only process SMS from known financial senders
                    if (BankSenderMap.isFinancialSms(sender)) {
                        smsList.add(RawSms(id = id, sender = sender, body = body, date = date))
                    }
                }
            }
        } finally {
            cursor?.close()
        }

        return smsList
    }
}
