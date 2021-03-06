package xyz.savvamirzoyan.trueithubtalks.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import xyz.savvamirzoyan.trueithubtalks.R
import xyz.savvamirzoyan.trueithubtalks.databinding.FragmentAccountBinding
import xyz.savvamirzoyan.trueithubtalks.factory.AccountViewModelFactory
import xyz.savvamirzoyan.trueithubtalks.ui.MainActivity

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding
    private lateinit var viewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Binding
        binding = FragmentAccountBinding.inflate(inflater, container, false)

        // ViewModel
        viewModel = ViewModelProvider(
            this,
            AccountViewModelFactory(context as MainActivity)
        ).get(AccountViewModel::class.java)

        setOnChangedUsernameListener()
        setOnChangedPictureUrl()

        return binding.root
    }

    private fun setOnChangedPictureUrl() {
        viewModel.pictureUrlLiveData.observe(viewLifecycleOwner) {
            Picasso.with(binding.imageViewUserPicture.context)
                .load(viewModel.pictureUrlLiveData.value)
                .placeholder(R.drawable.ic_account_circle)
                .into(binding.imageViewUserPicture)
        }
    }

    private fun setOnChangedUsernameListener() {
        viewModel.usernameLiveData.observe(viewLifecycleOwner) {
            binding.textViewUsername.text = it
        }
    }
}