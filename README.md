# CS595

Chat by Location

Tutorial included as ChatDemo2
Note: Currently, you must create your own firebase account and add the google-services.json file to your local android project.

```
//DB notes
db {
	users {
		userID {
			userName
			userPref*
			currentRoomID
			mutedUsers {
				userID
				...
			}
		
		}
		...
	}
	
	roomIdentities {
		roomID {
			name
			longg
			lat
			rad
			password
			ownerID
		}
		...
	}

	roomUsers {
		roomID {
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
		roomID {
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
	
```

