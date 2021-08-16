package com.evgeniykim.telcontacts

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.evgeniykim.telcontacts.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val CONTACT_PERMISSION_CODE = 1
    private val CONTACT_PICK_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Fab.setOnClickListener {
            if (checkPermission()) {
                pickContact()
            } else {
                requestPermission()
            }
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val permission = arrayOf(android.Manifest.permission.READ_CONTACTS)
        ActivityCompat.requestPermissions(this, permission, CONTACT_PERMISSION_CODE)
    }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, CONTACT_PICK_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CONTACT_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickContact()
            } else {
                requestPermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK) {
            if (requestCode == CONTACT_PICK_CODE) {
                binding.contactTv.text = ""
                
                val cursor1: Cursor
                val cursor2: Cursor?
                
                val uri = data!!.data
                cursor1 = contentResolver.query(uri!!, null, null, null, null)!!
                if (cursor1.moveToFirst()) {
                    val contactId = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID))
                    val contactName = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val contactThumbnail = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                    val idResults = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val idResultHold = idResults.toInt()
                    contactTv.append("ID: $contactId")
                    contactTv.append("\nName: $contactName")
                    
                    if (contactThumbnail != null) {
                        binding.imageV.setImageURI(Uri.parse(contactThumbnail))
                    } else {
                        binding.imageV.setImageResource(R.drawable.ic_baseline_perm_identity_24)
                    }
                    
                    if (idResultHold == 1) {
                        cursor2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                        null, null)
                        while (cursor2!!.moveToNext()) {
                            val contactNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            contactTv.append("\nPhone: $contactNumber")
                        }
                        cursor2.close()
                    }
                    cursor1.close()
                }
            }
        }
        else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

}