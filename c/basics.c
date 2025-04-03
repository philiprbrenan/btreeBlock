//------------------------------------------------------------------------------
// Basic routines in C
// Philip R Brenan at appaapps dot com, Appa Apps Ltd. Inc., 2022
//------------------------------------------------------------------------------
#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>
#include <assert.h>
#include <memory.h>
#include <string.h>
#include <stdarg.h>
//#include <execinfo.h>

static void say(char *format, ...)                                              // Say something
 {va_list p;
  va_start (p, format);
  int i = vfprintf(stderr, format, p);
  assert(i > 0);
  va_end(p);
  fprintf(stderr, "\n");
 }

static char *ssay(char *format, ...)                                            // Say something into a string
 {va_list p;
  va_start (p, format);
  char *result;
  int i = vasprintf(&result, format, p);
  assert(i > 0);
  va_end(p);
  return result;
 }

//void printTraceback()                                                           // Print a traceback
// {void *array[32];
//  size_t size = backtrace(array, sizeof(array)/sizeof(*array));
//  backtrace_symbols_fd(array, size, 2);
//  exit(1);
//}

static void stop(char *format, ...)                                             // Stop after saying something
 {va_list p;
  va_start (p, format);
  int i = vfprintf(stderr, format, p);
  assert(i > 0);
  va_end(p);
  fprintf(stderr, "\n");
//printTraceback();
  exit(0);
 }
