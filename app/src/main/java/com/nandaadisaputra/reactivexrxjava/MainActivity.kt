package com.nandaadisaputra.reactivexrxjava

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding2.widget.RxTextView
import com.nandaadisaputra.reactivexrxjava.databinding.ActivityMainBinding
import io.reactivex.Observable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Menambahkan data stream dari masing masing inputan, kemudian subscribe ke masing
        //masing stream tersebut.

        //Kita menggunakan RxTextView.textChanges(binding.edEmail) untuk membaca setiap perubahan
        // pada EditText dan mengubahnya menjadi data stream.
        val emailStream = RxTextView.textChanges(binding.edEmail)
            //operator skipInitialValue() untuk menghiraukan input awal. Hal ini bertujuan
            // supaya aplikasi tidak langsung menampilkan eror pada saat pertama kali dijalankan.
            .skipInitialValue()
            // Kita akan menggunakan map. Jika format tidak valid maka ia akan mengembalikan nilai TRUE.
            // Kemudian saat subscribe, Jika hasilnya TRUE kita memanggil fungsi showEmailExistAlert(it)
            // untuk menampilkan peringatannya
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe {
            showEmailExistAlert(it)
        }

        val passwordStream = RxTextView.textChanges(binding.edPassword)
            .skipInitialValue()
            .map { password ->
                password.length < 6
            }
        passwordStream.subscribe {
            showPasswordMinimalAlert(it)
        }
        //Pada bagian inputan confirmation password akan sedikit berbeda.
        // Karena mengecek pada dua inputan sekaligus, yaitu pada
        // ed_password dan ed_confirm_password. Sehingga perlu menggabungkan
        // dua data tersebut dengan operator merge.
        val passwordConfirmationStream = Observable.merge(
            RxTextView.textChanges(binding.edPassword)
                .map { password ->
                    password.toString() != binding.edConfirmPassword.text.toString()
                },
            RxTextView.textChanges(binding.edConfirmPassword)
                .map { confirmPassword ->
                    confirmPassword.toString() != binding.edPassword.text.toString()
                }
        )
        passwordConfirmationStream.subscribe {
            showPasswordConfirmationAlert(it)
        }
        //Dengan operator combineLatest Kita akan membaca ketiga data stream tersebut untuk menentukan
        //apakah tombol diaktifkan atau tidak
        val invalidFieldsStream = Observable.combineLatest(
            emailStream,
            passwordStream,
            passwordConfirmationStream
        ) { emailInvalid: Boolean, passwordInvalid: Boolean, passwordConfirmationInvalid: Boolean ->
            !emailInvalid && !passwordInvalid && !passwordConfirmationInvalid
        }
        //Jika pada operator combineLatest kita menggabungkan dan mengubah data di dalamnya.
        // Maka pada operator merge kita hanya menggabungkan datanya saja.
        invalidFieldsStream.subscribe { isValid ->
            if (isValid) {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.purple_500
                    )
                )
            } else {
                binding.btnRegister.isEnabled = false
                binding.btnRegister.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.darker_gray
                    )
                )
            }
        }
    }

    private fun showEmailExistAlert(isNotValid: Boolean) {
        binding.edEmail.error = if (isNotValid) getString(R.string.email_not_valid) else null
    }

    private fun showPasswordMinimalAlert(isNotValid: Boolean) {
        binding.edPassword.error = if (isNotValid) getString(R.string.password_not_valid) else null
    }

    private fun showPasswordConfirmationAlert(isNotValid: Boolean) {
        binding.edConfirmPassword.error =
            if (isNotValid) getString(R.string.password_not_same) else null
    }
}