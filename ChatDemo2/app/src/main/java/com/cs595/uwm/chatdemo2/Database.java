package com.cs595.uwm.chatdemo2;

/**
 * Created by Lowell on 3/20/2017.
 */

public class Database {

    public enum DB {
        USER,
        ROOM_ID,
        ROOM_USERS,
        ROOM_MESSAGES,
        ;

        public enum USER {
            ;

        }

        public enum ROOM_ID {
            ROOM
            ;

            public enum ROOM {
                name,
                longg,
                lat,
                ;

            }

        }

        public enum ROOM_USERS {
            ROOM
            ;

            public enum ROOM {
                MESSAGES,
                ;
            }

        }

        public enum ROOM_MESSAGES {

        }

    }
}
