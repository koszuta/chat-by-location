# CS595
<<<<<<< HEAD

Chat by Location



//DB notes
db {
	users {
		user {
			userID
			userName
			userPref*
			currentRoom {
				roomID
				isOwner
			}
			mutedUsers {
				userID
				...
			}
		
		}
		...
	}
	
	roomIdentities {
		room {
			roomID
			name
			long
			lat
			rad
		}
		...
	}

	roomUsers {
		room {
			roomID
			ownerID
			users {
				userID
				...
			}
			bannedUsers {
				userID
				...
			}
		}
		...
	}
	
	roomMessages {
		room {
			roomID
			messages {
				message {
					userID
					styleInfo
					time
					text
				}
				...
			}
		}
		...
	}
}

createUser -> 
	write to users/user(userID)
	
setUserPref* -> 
	write to users/user(userID)/userPref*
	
createRoom -> 
	write to roomIdentities/room(roomID)
	write to roomMessages/room(roomID)
	write to roomUsers/room(roomID)
	write to roomUsers/room(roomID)/users
	write to users/user(userID)/currentRoom
	
getRooms -> 
	read from roomIdentities/
 
joinRoom -> 
	read from roomUsers/room(roomID)/bannedUsers
	write to users/user(userID)/currentRoom
	write to roomUsers/room(roomID)/users
	
leaveRoom -> 
	write to users/user(userID)/currentRoom
	write to roomUsers/room(roomID)/users

changeOwner -> (leaveRoom and isOwner)
	write to roomUsers/room(roomID)

getRoomUsers -> 
	read from roomUsers/room(roomID)
	
getUserInfo ->
	read from users/user(userID)
	
muteUser ->
	write to users/user(userID)/mutedUsers

banUser -> 
	write to roomUsers/room(roomID)/users
	write to roomUsers/room(roomID)/bannedUsers
	write to users/user(userID)/currentRoom

sendMessage ->
	write to roomMessages/room(roomID)
	
listenMessage -> 
	subscribe to roomMessages/room(roomID)


=======
Tutorial included as ChatDemo2
>>>>>>> 3c5670302bf86ff3f1e245155197caba2ea27d92
