//------------------------------------------------------------------------------
// Stuck definition
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
#ifndef [stuck_maxSize
  #define stuck_maxSize 20                                                      // The maximum number of entries in the stuck.
#endif

#ifndef stuck_keyType
  #define stuck_keyType int                                                     // The type of a key
#endif

#ifndef stuck_dataType
  #define stuck_dataType int                                                    // The type of a data item in a stuck
#endif

typedef struct                                                                  // Definition of a stuck
 {int currentSize;                                                              // Current size of the stuck
  stuck_keyType  keys[stuck_maxSize];                                           // Keys
  stuck_dataType data[stuck_maxSize];                                           // Data
 } Stuck;
