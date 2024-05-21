package com.example.demo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObjects


class RecommendVM : ViewModel() {
    private val userLD = MutableLiveData<List<User>>(emptyList())
    private var listener: ListenerRegistration? = null

    init {
        listener = USERS.addSnapshotListener { snap, _ ->
            userLD.value = snap?.toObjects()
            updateNewFriendResult()
        }
    }

    override fun onCleared() {
        listener?.remove()
    }

    fun init() = Unit

    fun getUserLD() = userLD

    fun getAll() = userLD.value ?: emptyList()

    fun get(id: String) = getAll().find { it.id == id }

    fun set(user: User) {
        USERS.document(user.id).set(user);
    }


    private val recommendLD = MutableLiveData<List<User>>()
    private val newFriendsLD = MutableLiveData<List<User>>()
    fun getRecommendLD() = recommendLD


    fun updateNewFriendResult() {
        var list = newFriendsLD.value ?: emptyList()

        recommendLD.value = list
    }

   fun skip(id: String) {
        val list = recommendLD.value?.toMutableList() ?: mutableListOf()
        list.remove(get(id))
        recommendLD.value = list
    }

    fun sendRequest(userId: String, friendId: String):Boolean {
        val target = get(friendId) ?: return false

        val list = recommendLD.value?.toMutableList() ?: mutableListOf()
        list.remove(get(friendId))
        recommendLD.value = list
        // Check if userId already exists in friendRequestFrom
        if (target.friendRequestFrom.contains(userId)) {
            return false
        }

        // Add friendId to currentUser's friends list
        val updatedRequestList = target.friendRequestFrom.toMutableList()
        updatedRequestList.add(userId)
        target.friendRequestFrom = updatedRequestList

        set(target)
        return true
    }

    fun fetchNewFriends(userId: String) {
        val user = get(userId) ?: return
        val friends: List<String> = user.friends
        val nonFriendsList = mutableListOf<User>()
        for (potentialFriend in getAll()) {
            if (potentialFriend.id !in friends && potentialFriend.id != userId) {
                nonFriendsList.add(potentialFriend)
            }
        }

        newFriendsLD.value = nonFriendsList
        recommendLD.value = nonFriendsList
    }





    fun getMutualFriends(currentUserID:String, friendID:String):String{
        var list = ""
        val currentUser = get(currentUserID) ?: return list
        val friend = get(friendID) ?: return list

        for (f in currentUser.friends){
            if (friend.friends.contains(f)){
                val mutual = get(f) ?: return list
                list += mutual.name + ", "
            }
        }


        return list
    }

}