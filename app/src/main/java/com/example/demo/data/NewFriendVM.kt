package com.example.demo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObjects


class NewFriendVM:ViewModel() {
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


    private val newFriendResultLD = MutableLiveData<List<User>>()
    private val newFriendsLD = MutableLiveData<List<User>>()
    private var name = ""
    fun getNewFriendResultLD() = newFriendResultLD

    fun searchNewFriend(name: String) {
        this.name = name
        updateNewFriendResult()
    }

    fun updateNewFriendResult() {
        var list = newFriendsLD.value ?: emptyList()

        list = list.filter {
            it.name.contains(name, true)
        }

        newFriendResultLD.value = list
    }

    fun acceptFriendRequest(userId: String, friendId: String) {
        val user = get(userId) ?: return
        val updatedFriendsList = user.friends.toMutableList()
        updatedFriendsList.add(friendId)
        user.friends = updatedFriendsList

        // Remove friendId from user.friendRequestFrom
        val updatedFriendRequestsFrom = user.friendRequestFrom.toMutableList()
        updatedFriendRequestsFrom.remove(friendId)
        user.friendRequestFrom = updatedFriendRequestsFrom

        set(user)
        fetchNewFriends(userId)
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
        newFriendResultLD.value = nonFriendsList
    }


    private fun emailExists(email: String) = userLD.value?.any { it.email == email } ?: false

    fun validate(user: User, insert: Boolean = true): String {
        val regexEmail = Regex("""^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$""")
        var e = ""

        if (insert) {
            e += if (user.email == "") "- Email required.\n"
            else if (!user.email.matches(regexEmail)) "- Email format invalid.\n"
            else if (user.email.length > 100) "- Email too long (max 100 chars).\n"
            else if (emailExists(user.email)) "- Email duplicated.\n"
            else ""
        }

        e += if (user.password == "") "- Password required.\n"
        else if (user.password.length < 5) "- Password too short (min 5 chars).\n"
        else if (user.password.length > 100) "- Password too long (max 100 chars).\n"
        else ""

        e += if (user.name == "") "- Name required.\n"
        else if (user.name.length < 3) "- Name too short (min 3 chars).\n"
        else if (user.name.length > 100) "- Name too long (max 100 chars).\n"
        else ""

        e += if (user.photo.toBytes().isEmpty()) "- Photo required.\n"
        else ""


        // add more validation here

        return e
    }
}