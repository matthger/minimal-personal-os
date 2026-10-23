package com.tisosoft.minimalpersonalos.data

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.content.ContentProviderOperation
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

data class Contact(
    val id: Long,
    val displayName: String,
    val phoneNumber: String,
    val photoUri: Uri?,
    val isFavorite: Boolean
)

class ContactRepository(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    fun getContactsWithFavoritesSorted(): Flow<List<Contact>> = flow {
        val allContacts = getAllContactsFromProvider()
        val favoriteIds = settingsRepository.getFavoritesSync()

        val contactsWithFavoriteFlag = allContacts.map { contact ->
            contact.copy(isFavorite = contact.id in favoriteIds)
        }

        // Separate favorites and others
        val favorites = contactsWithFavoriteFlag.filter { it.isFavorite }
        val others = contactsWithFavoriteFlag.filter { !it.isFavorite }
            .sortedBy { it.displayName }

        emit(favorites + others)
    }.flowOn(Dispatchers.IO)

    fun getAllContactsFromProvider(): List<Contact> {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return emptyList()
        }

        val contacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI
        )

        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )

        cursor?.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val displayName =
                    c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val photoUri =
                    c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))

                val phoneNumber = getPhoneNumber(id)

                if (phoneNumber.isNotEmpty()) {
                    contacts.add(
                        Contact(
                            id = id,
                            displayName = displayName,
                            phoneNumber = phoneNumber,
                            photoUri = photoUri?.toUri(),
                            isFavorite = false
                        )
                    )
                }
            }
        }

        return contacts
    }

    private fun getPhoneNumber(contactId: Long): String {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return ""
        }

        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId.toString()),
            null
        )

        cursor?.use { c ->
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
        }

        return ""
    }

    suspend fun addContact(name: String, phoneNumber: String): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return try {
            withContext(Dispatchers.IO) {
                val ops = arrayListOf<ContentProviderOperation>()

                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build()
                )

                // Add Name
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                        .build()
                )

                // Add Phone Number
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .withValue(
                            ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                        )
                        .build()
                )

                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateContact(contactId: Long, name: String, phoneNumber: String): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return try {
            withContext(Dispatchers.IO) {
                val ops = arrayListOf<ContentProviderOperation>()

                // Update Name
                ops.add(
                    ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(
                            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                            arrayOf(
                                contactId.toString(),
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                            )
                        )
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                        .build()
                )

                // Update Phone Number
                ops.add(
                    ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(
                            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                            arrayOf(
                                contactId.toString(),
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                            )
                        )
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .build()
                )

                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteContact(contactId: Long): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return try {
            withContext(Dispatchers.IO) {
                val uri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                    contactId.toString()
                )
                // Note: For simple ID based deletion we can use the raw contact URI or the contact URI
                // The most reliable way to delete the whole contact by ID is using the content resolver on the contact URI
                val deletedCount = context.contentResolver.delete(
                    ContactsContract.RawContacts.CONTENT_URI,
                    "${ContactsContract.RawContacts.CONTACT_ID} = ?",
                    arrayOf(contactId.toString())
                )
                deletedCount > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
