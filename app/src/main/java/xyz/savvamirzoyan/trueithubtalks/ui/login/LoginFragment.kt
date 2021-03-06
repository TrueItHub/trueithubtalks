package xyz.savvamirzoyan.trueithubtalks.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import timber.log.Timber
import xyz.savvamirzoyan.trueithubtalks.databinding.FragmentLoginBinding
import xyz.savvamirzoyan.trueithubtalks.factory.LoginViewModelFactory
import xyz.savvamirzoyan.trueithubtalks.interfaces.IAuthenticateActivity
import xyz.savvamirzoyan.trueithubtalks.ui.AuthenticateActivity
import xyz.savvamirzoyan.trueithubtalks.ui.toInt

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView() called")

        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        // ViewModel
        viewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(context as AuthenticateActivity)
        ).get(LoginViewModel::class.java)

        binding.editTextName.text.insert(0, viewModel.userName)
        binding.editTextPassword.text.insert(0, viewModel.userPassword)

        binding.buttonLogin.isEnabled = viewModel.isLoginButtonEnabled
        setAlphaByBoolean(binding.buttonLogin, viewModel.isLoginButtonEnabled)

        setLoginClickListener()
        setSignUpClickListener()
        setOnChangedNameListener()
        setOnChangedPasswordListener()
        setTokenObserver()

        viewModel.retrieveTokenFromSharedPreferences()

        return binding.root
    }

    private fun setSignUpClickListener() {
        binding.textViewSignUp.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    private fun setOnChangedNameListener() {
        binding.editTextName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateNameFilled(s.toString())

                binding.buttonLogin.isEnabled = viewModel.isLoginButtonEnabled
                setAlphaByBoolean(binding.buttonLogin, viewModel.isLoginButtonEnabled)
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
    }

    private fun setOnChangedPasswordListener() {
        binding.editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updatePasswordFilled(s.toString())

                binding.buttonLogin.isEnabled = viewModel.isLoginButtonEnabled
                setAlphaByBoolean(binding.buttonLogin, viewModel.isLoginButtonEnabled)
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
    }

    private fun setAlphaByBoolean(view: View, value: Boolean) {
        view.alpha = 0.3F + 0.7F * value.toInt()
    }

    private fun setLoginClickListener() {
        binding.buttonLogin.setOnClickListener {
            viewModel.sendCredentials(
                binding.editTextName.text.toString(),
                binding.editTextPassword.text.toString()
            )
        }
    }

    private fun setTokenObserver() {
        viewModel.tokenLiveData.observe(viewLifecycleOwner) {
            Timber.i("token: '$it'")
            // invalid credentials
            if (it == "") {
                binding.textViewInvalidCredentials.visibility = View.VISIBLE
            } else {
                binding.textViewInvalidCredentials.visibility = View.INVISIBLE
                (activity as IAuthenticateActivity).moveToMainActivity()
            }
        }
    }
}
