package cn.troph.tomon.ui.chat.members

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.structures.User

class MemberViewModel: ViewModel() {
    private val memberLiveData = MutableLiveData<MutableList<User>>()

}