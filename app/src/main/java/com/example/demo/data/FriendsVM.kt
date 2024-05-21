package com.example.demo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObjects


class FriendsVM : ViewModel() {
    private val userLD = MutableLiveData<List<User>>(emptyList())
    private var listener: ListenerRegistration? = null

    init {
        listener = USERS.addSnapshotListener { snap, _ ->
            userLD.value = snap?.toObjects()
            updateResult()
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


    private val resultLD = MutableLiveData<List<User>>()
    private val requestToLD = MutableLiveData<List<User>>()
    private val requestFromLD = MutableLiveData<List<User>>()
    private val newFriendsLD = MutableLiveData<List<User>>()
    private val newFriendResultLD = MutableLiveData<List<User>>()
    private val friendsLD = MutableLiveData<List<User>>()
    private var name = ""
    fun getResultLD() = resultLD
    fun getRequestToLD() = requestToLD
    fun getRequestFromLD() = requestFromLD


    // Friend List

    fun search(name: String) {
        this.name = name
        resultLD.value = friendsLD.value
        updateResult()
    }

    fun sortOnline() {
        var list = friendsLD.value ?: emptyList()
        val onlineUser = list.filter { it.status == "Online" }
        resultLD.value = onlineUser
    }

    fun updateResult() {
        var list = friendsLD.value ?: emptyList()

        // TODO(12A): Search by name, filter by categoryId
        list = list.filter {
            it.name.contains(name, true)
        }

        resultLD.value = list

    }


    // New Friends
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

    fun acceptFriendRequest(userId: String, friendId: String):Boolean {
        val user = get(userId) ?: return false
        val target = get(friendId) ?: return false

        val updatedFriendsList = user.friends.toMutableList()
        updatedFriendsList.add(friendId)
        user.friends = updatedFriendsList

        val updatedTargetFriendsList = target.friends.toMutableList()
        updatedTargetFriendsList.add(userId)
        target.friends = updatedTargetFriendsList

        // Remove friendId from user.friendRequestFrom
        val updatedFriendRequestsFrom = user.friendRequestFrom.toMutableList()
        updatedFriendRequestsFrom.remove(friendId)
        user.friendRequestFrom = updatedFriendRequestsFrom

        val updatedTargetFriendRequestsFrom = target.friendRequestFrom.toMutableList()
        updatedTargetFriendRequestsFrom.remove(userId)
        target.friendRequestFrom = updatedTargetFriendRequestsFrom

        set(user)
        set(target)
        fetchNewFriends(userId)
        fetchFriends(userId)
        fetchRequests(userId)

        return true
    }

    fun deleteFriendRequest(userId: String, friendId: String):Boolean {
        val user = get(userId) ?: return false

        // Remove friendId from user.friendRequestFrom
        val updatedFriendRequestsFrom = user.friendRequestFrom.toMutableList()
        updatedFriendRequestsFrom.remove(friendId)
        user.friendRequestFrom = updatedFriendRequestsFrom

        set(user)
        fetchNewFriends(userId)
        fetchFriends(userId)
        fetchRequests(userId)

        return true
    }

    fun sendRequest(userId: String, friendId: String):Boolean {
        val target = get(friendId) ?: return false

        // Check if userId already exists in friendRequestFrom
        if (target.friendRequestFrom.contains(userId)) {
            return false
        }

        // Add friendId to currentUser's friends list
        val updatedRequestList = target.friendRequestFrom.toMutableList()
        updatedRequestList.add(userId)
        target.friendRequestFrom = updatedRequestList

        set(target)
        fetchNewFriends(userId)
        fetchFriends(userId)
        fetchRequests(userId)
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
        newFriendResultLD.value = nonFriendsList
    }


    fun fetchFriends(userId: String) {
        val user = get(userId) ?: return
        val friends:List<String> = user.friends
        val friendsList = mutableListOf<User>()
        for (potentialFriend in getAll()) {
            if (potentialFriend.id in friends) {
                friendsList.add(potentialFriend)
            }
        }

        friendsLD.value = friendsList
        resultLD.value = friendsList
    }

    fun fetchRequests(userId: String) {
        val user = get(userId) ?: return
        val requestsSent: List<String> = user.friendRequestTo
        val requestList = mutableListOf<User>()
        for (potentialRequest in getAll()) {
            if (potentialRequest.id in requestsSent) {
                requestList.add(potentialRequest)
            }
        }

        requestToLD.value = requestList


        val requestsReceived: List<String> = user.friendRequestFrom
        val requestReceivedList = mutableListOf<User>()
        for (potentialReceivedRequest in getAll()) {
            if (potentialReceivedRequest.id in requestsReceived) {
                requestReceivedList.add(potentialReceivedRequest)
            }
        }

        requestFromLD.value = requestReceivedList
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

    fun deleteFriend(currentUserID:String, friendID:String){
        val currentUser = get(currentUserID) ?: return
        val friend = get(friendID) ?: return

        val updatedFriendsList = currentUser.friends.toMutableList()
        updatedFriendsList.remove(friendID)
        currentUser.friends = updatedFriendsList

        val updatedFriendList = friend.friends.toMutableList()
        updatedFriendList.remove(currentUserID)
        friend.friends = updatedFriendList

        set(currentUser)
        set(friend)
        fetchNewFriends(currentUserID)
        fetchFriends(currentUserID)
        fetchRequests(currentUserID)
    }

    fun friendRecommender(currentUserID:String):List<User>{
        val currentUser = get(currentUserID) ?: return emptyList()
        val friends = currentUser.friends
        val friendList = mutableListOf<User>()
        for (f in getAll()){
            if (f.id !in friends && f.id != currentUserID){
                friendList.add(f)
            }
        }
        return friendList
    }
}