package com.skripsi.estock.ui.profile

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivityProfileBinding
import com.skripsi.estock.databinding.DialogCriteriaBinding
import com.skripsi.estock.databinding.DialogEditNameBinding
import com.skripsi.estock.databinding.DialogResetPasswordBinding
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.login.LoginActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var progresDialog: ProgressDialog

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val usersCollection = Firebase.firestore.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        progresDialog = ProgressDialog(this)
        progresDialog.setMessage("Silahkan Tunggu")

        val fullName = binding.tvFullName
        val email = binding.tvEmail

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            fullName.text = firebaseUser.displayName
            email.text = firebaseUser.email
        }

        if (userId != null) {
            usersCollection.document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Retrieve the role from the document
                        val role = documentSnapshot.getLong("role")
                        if (role != null) {
                            Log.d("TAG_Role", "role: $role")
                            // Use '==' for comparison, '=' is for assignment
                            if (role.toInt() == 1) {
                                binding.tvRole.text = "Admin"
                            } else if (role.toInt() == 2) {
                                binding.tvRole.text = "Investor Awam"
                            } else {
                                Log.e("TAG", "Invalid role value: $role")
                            }
                        } else {
                            Log.e("TAG", "Role field is null")
                        }
                    } else {
                        Log.e("TAG", "User document does not exist")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TAG", "Error getting user document: $e")
                }
        } else {
            Log.e("TAG", "User ID is null")
        }

        binding.apply {
            btnLogout.setSafeOnClickListener {
                progresDialog.setTitle("Keluar Akun")
                firebaseAuth.signOut()
                progresDialog.show()
                startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                finish()
            }
            btnUpdate.setSafeOnClickListener {
                showResetPaswordDialog()
            }
            btnEditName.setSafeOnClickListener {
                showEditNamaDialog()
            }
        }

    }

    private fun showEditNamaDialog() {
        val dialogBinding: DialogEditNameBinding? =
            DialogEditNameBinding.inflate(LayoutInflater.from(this), null, false)

        val customDialog = AlertDialog.Builder(this, 0).create()

        customDialog.apply {
            setView(dialogBinding?.root)
            setCancelable(false)
        }.show()

        dialogBinding?.apply{
            btnSave.setSafeOnClickListener {
                val name:String = etFillName.text.toString().trim()

                if (name.isEmpty()){
                    etName.error = "Kata Sandi harus diisi / minimal 6 huruf"
                    etName.requestFocus()
                    return@setSafeOnClickListener
                }

                showEditNameConfirmation(name)
                customDialog.dismiss()
            }
            btnCancel.setSafeOnClickListener {
                customDialog.dismiss()
            }
        }
    }

    private fun showEditNameConfirmation(name: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Rubah Nama")
        builder.setMessage("Apakah kamu yakin akan merubah nama?")

        // Set up the buttons
        builder.setPositiveButton("Simpan") { dialog, which ->
            // User clicked Delete button
            val user = firebaseAuth.currentUser
            progresDialog.setTitle("Rubah Nama")
            val userUpdateProfile = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            user?.let {
                user.updateProfile(userUpdateProfile)
                    .addOnCompleteListener{
                    if (it.isSuccessful){
                        Toast.makeText(this@ProfileActivity, "Nama Berhasil Dirubah", Toast.LENGTH_SHORT).show()
                        recreate()
                    }else{
                        Toast.makeText(this@ProfileActivity, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                        progresDialog.dismiss()
                    }
                }
            }
        }

        builder.setNegativeButton("Batal") { dialog, which ->
            // User clicked Cancel button
            Toast.makeText(this, "Batal Menghapus", Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }

    private fun showResetPaswordDialog() {
        val dialogBinding: DialogResetPasswordBinding? =
            DialogResetPasswordBinding.inflate(LayoutInflater.from(this), null, false)

        val customDialog = AlertDialog.Builder(this, 0).create()

        customDialog.apply {
            setView(dialogBinding?.root)
            setCancelable(false)
        }.show()

        dialogBinding?.apply{
           btnSave.setSafeOnClickListener {
               val passwordNew:String = etFillPasswordNew.text.toString().trim()
               val passwordConf:String = etFillPasswordConf.text.toString().trim()

               if (passwordNew.isEmpty()||passwordNew.length < 6){
                   etPasswordNew.error = "Kata Sandi harus diisi / minimal 6 huruf"
                   etPasswordNew.requestFocus()
                   return@setSafeOnClickListener
               }
               if (passwordConf.isEmpty()||passwordConf.length < 6){
                   etPasswordConf.error = "Kata Sandi Tidak Sama"
                   etPasswordConf.requestFocus()
                   return@setSafeOnClickListener
               }
               showResetPasswordConfirmation(passwordNew)
               customDialog.dismiss()
           }
            btnCancel.setSafeOnClickListener {
                customDialog.dismiss()
            }
        }


    }
    private fun showResetPasswordConfirmation(passwordNew:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Rubah Kata Sandi")
        builder.setMessage("Apakah kamu yakin akan mengubah kata sandi?")

        // Set up the buttons
        builder.setPositiveButton("Simpan") { dialog, which ->
            // User clicked Delete button
            val user = firebaseAuth.currentUser
            progresDialog.setTitle("Rubah Kata Sandi")
            progresDialog.show()
            user?.let {
                user.updatePassword(passwordNew).addOnCompleteListener{
                    if (it.isSuccessful){
                        Toast.makeText(this@ProfileActivity, "Kata Sandi Berhasil Dirubah", Toast.LENGTH_SHORT).show()

                        progresDialog.dismiss()
                    }else{
                        Toast.makeText(this@ProfileActivity, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                        progresDialog.dismiss()
                    }
                }
            }
        }

        builder.setNegativeButton("Batal") { dialog, which ->
            // User clicked Cancel button
            Toast.makeText(this, "Batal Menghapus", Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }
}

