/*----------------------------------------------+
 |												|
 |	ivDefine.h - Basic Definitions				|
 |												|
 |		Copyright (c) 1999-2009, iFLYTEK Ltd.	|
 |		All rights reserved.					|
 |												|
 +----------------------------------------------*/
#ifndef IFLYTEK_VOICE__ESRDEFINE__H
#define IFLYTEK_VOICE__ESRDEFINE__H

#include "ivDefine.h"

#define IV_FIXED_ERRORCODE		(1)
#define ivVolatile	IV_VOLATILE

/* ״ֵ̬ */
#if IV_FIXED_ERRORCODE
typedef ivInt32		ivStatus;
#else
typedef ivCStr		ivStatus;
#endif

/* User OS Base function and interface */
/* Memory supply */
typedef ivPointer (ivProc PCBRealloc)(
									  ivPointer pUser,
									  ivPointer p,			/* Memory pointer to be reallocate, ivNull Means to Alloc new memory */
									  ivSize nSize			/* Memory Size wanted, in bytes */
									  );						/* return : Memory pointer reallocated or allocated */
typedef void (ivProc PCBFree)(
							  ivPointer pUser,
							  ivPointer p				/* Memory pointer to be freed */
							  );

/* File */
typedef ivPointer (ivProc PCBMapFile)(
									  ivPointer pUser,
									  ivCStr lpFileName		/* File name */
									  );						/* return : Memory pointer of mapped data */
typedef void (ivProc PCBUnmapFile)(
								   ivPointer pUser,
								   ivPointer pImg			/* Memory pointer of mapped data */
								   );

#define ivModRead	 (1)	/* Open file for read */
#define ivModWrite	 (2)	/* Open file for write */
/*#define ivModRW      (3)	*//* Open file for read & write */
#define ivResFile    (1)	/* File deployed as Resource */
#define ivSerialFile (2)	/* File deployed as Serial data, Persistent while device shutdown */
#define ivCommonFile (3)	/* File deployed as Common data */

#define  FILE_POS_CURRENT		((ivUInt32)(~((ivUInt32)(0))))

typedef ivHandle (ivProc PCBOpenFile)(
									  ivPointer pUser,
									  ivCStr lpFileName,		/* File Name */
									  ivInt enMod,			/* Open file mod */
									  ivInt enType			/* File deployed type */
									  );						/* return : Handle of opened file, ivNull if file not exist */
/* Note: You can deploy different type file to different place/directory
You must complete the full path name of the file name by yourself, And at
the time enMod is referred to indicate the directory info, the relationship 
is as any as your wish! */
typedef ivBool (ivProc PCBCloseFile)(
									 ivPointer pUser,
									 ivHandle hFile			/* Handle of opened file */
									 );
typedef ivBool (ivProc PCBReadFile)(
									ivPointer	pUser,
									ivHandle	hFile,		/* Handle of opened file */
									ivPByte		pBuffer,	/* Buffer to Received data */
									ivUInt32	iPos,		/* Offset of file to be read */
									ivSize		nSize		/* Size of data to read, in bytes */
									);						/* return : ivTrue if succeed, ivFalse either */
typedef ivBool (ivProc PCBWriteFile)(
									 ivPointer	pUser,
									 ivHandle	hFile,		/* Handle of opened file */
									 ivPCByte	pBuffer,	/* Buffer of data to write */
									 ivUInt32	iPos,		/* Offset of file to be write */
									 ivSize		nSize		/* Size of data to write, in bytes */
									 );						/* return : ivTrue if succeed, ivFalse either */

typedef ivStatus (ivProc PCBMsgProc)(
									 ivPointer	pUser,
									 ivHandle	hObj,		/* Handle to the object */
									 ivUInt32	uMsg,		/* Message ID, Specifies the message */
									 ivUInt32	wParam,		/* Specifies additional message information.
															The contents of this parameter depend on the value of the uMsg parameter */
															ivCPointer	lParam		/* Specifies additional message information.
																					The contents of this parameter depend on the value of the uMsg parameter */
																					); 

typedef struct tagUserOS
{
	ivSize			nSize;			/* Size of this structure */

	ivStrA			lpszLicence;	/* SN code string */

	ivBool			bCheckResource;	/* Indicate whether to check Resource while load, The check will
									spend some CPU operation, You can set this switch on while develop,
									But Do not forget to turn-off it before release */

	/* Memory deploy and supply interface */
	ivPointer		pRAM;			/* Memory deployed static with one piece of buffer,
									if pfnRealloc was implemented, this is NOT necessary */
	ivSize			nRAMSize;		/* Memory size, in bytes, of buffer that pRAM pointed */

	ivPointer		pFastRAM;		/* Fast RAM supplied */
	ivSize			nFastRAMSize;	/* Fast RAM size, in bytes */

	ivPointer		pPersisRAM;		/* If do NOT implement PCBWriteFile callback, You'd better
									Offer a Persistent buffer to record data Across the Object
									life Scope, This buffer should keep valid before device 
									been Shutdown, That is,Should be a global buffer */
	ivSize			nPersisRAMSize;	/* Persistent RAM size, in bytes */

	PCBRealloc		lpfnRealloc;	/* Memory allocate interface, You can only 
									implement Alloc either than Re-, 
									That is, if(ivNull!=p) You can return ivNull. */
	PCBFree			lpfnFree;		/* Memory free interface */

	/* File visit interface */
	PCBMapFile		lpfnMapFile;	/* Can be ivNull if NOT necessary */
	PCBUnmapFile	lpfnUnmapFile;	/* If there isn's OS resource problem, It is 
									allowed not to implement together with pfnMapFile. 
									For example: pfnMapFile return fixed Flash memory 
									address; In this case, pfnUnmapFile Can be ivNull */

	PCBOpenFile		lpfnOpenFile;	/* Open file */
	PCBCloseFile	lpfnCloseFile;	/* Close file */
	PCBReadFile		lpfnReadFile;	/* File read Interface */
	PCBWriteFile	lpfnWriteFile;	/* File Write Interface */

	/* Message handler */
	PCBMsgProc		lpfnMsgProc;	/* Message process Interface */

	ivPointer		pUser;			/* User associated pointer */
}TUserOS,ivPtr PUserOS,ivPtr ivPUserOS;
typedef TUserOS ivCPtr ivPCUserOS;


/* Message ID description */
/* OS Schedule an Cooperation */
#define ivMsg_ToSleep					(0x101)
/*----------------------------------------------------------------------------------
Demand application to call OS's Sleep mechanism if there is.
This message is sent when operation of the EsrObject come to an idle status.
You'd better to process this message, as it can optimize the CPU's occupancy.
Return ivErr_FALSE, this message will not be sent anymore. Otherwise,you'd
return ivErr_OK to keep on this message to be sent.
Parameter:
wParam : [as ivUInt32] Sleep time Demanded in millisecond
lParam : NA
Return:
ivErr_OK	: Support and processed.
ivErr_FALSE	: Ignore and this message will not be sent anymore.
==================================================================================*/
#define ivMsg_ToCoopOS					(0x102)
/*----------------------------------------------------------------------------------
Demand application to call OS's Cooperation mechanism.
This message is sent constantly whenever a portion of operation of the EsrObject
was done. On this message, you can do the OS 's Cooperation mechanism. On a
Preemptive OS such as win32os, you do not need process this message, just re-
turn ivErr_FALSE, then this message will not be sent anymore. Otherwise,you'd
return ivErr_OK to keep on this message to be sent.
On Cooperative OS, This message must be processed and return ivErr_OK!
Parameter: NA
Return:
ivErr_OK	: Support and processed.
ivErr_FALSE	: Ignore and this message will not be sent anymore.
==================================================================================*/

#define ivMsg_ToEnterCriticalSection	(0x103)
#define ivMsg_ToLeaveCriticalSection	(0x104)
/*----------------------------------------------------------------------------------
Demand application to call OS's CriticalSection mechanism if there is.
Whenever the EsrObject want to access data protectively, this pair of message
will be sent, if the OS do not support CriticalSection mechanism, just return
ivErr_FALSE, and this message will not be sent anymore.
CriticalSection Should SUPPORT REENTER by the same thread!
On Preemptive OS, This message must be processed and return ivErr_OK!
Parameter:	
wParam : NA
lParam : [as ivPointer] User data binded to the object, Usually the Critical-
-Section data was binded as one member of User data
Return:
ivErr_OK	: Support and processed.
ivErr_FALSE	: Ignore and this message will not be sent anymore.
==================================================================================*/

/* LOG mechanism */
#define ivMsg_LOG						(0x110)
/*----------------------------------------------------------------------------------
LOG data produced by the EsrObject
This message can be ignore if you do not care it
Parameter:
wParam : [as ivSize] Size of the LOG data in bytes
lParam : [as ivPByte] The LOG data
Return: NA
==================================================================================*/

/* EsrObject Create & Destroy */
#define ivMsg_Create					(0x200)
/*----------------------------------------------------------------------------------
Notify application that the EsrObject was Created.
This message is sent when the EsrObject be created, Here the application can 
call the other initializing function, such as set EsrObject's parameter, And
Allocate and initialize the data binded with the EsrObject
Parameter: NA
Return:
ivErr_OK	: All OK.
Other		: There's some error.
==================================================================================*/
#define ivMsg_Destroy					(0x201)
/*----------------------------------------------------------------------------------
Notify application that the EsrObject will be Destroyed.
This message is sent when the EsrObject will be Destroyed, Here the application
can do some anti-initializing work, such as anti-initialize and free the 
data binded with the EsrObject.
When you Destroy the EsrObject by call function EsrDestroy, the EsrObject may
NOT be really Destroyed immediately, so that the anti-initializing work of
application binded with the EsrObject can only be process here.
Parameter: NA
Return: NA
==================================================================================*/

/* Error information */
#define ivMsg_Error				(0xffff)
/*----------------------------------------------------------------------------------
Notify application that an Error raised.
Parameter: 
wParam : NA
lParam : [as ivStrA] The error information
return: NA
==================================================================================*/
#endif /* !IFLYTEK_VOICE__ESRDEFINE__H */
