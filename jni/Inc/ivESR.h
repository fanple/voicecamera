/*****************************************************************
//  ivESR   version:  1.0   ? date: 2010/08/26
//  -------------------------------------------------------------
//  API
//  -------------------------------------------------------------
//  Copyright (C) 2010 - All Rights Reserved
// ***************************************************************
// 	Sheng Chen
// ***************************************************************/

#ifndef ES_TEAMM__2007_08_08__ESR__H
#define ES_TEAMM__2007_08_08__ESR__H

#include "ivEsrDefine.h"
#include "ivErrorCode.h"

/* Message ID description */

/* Audio Control */
#define ivMsg_ToStartAudioRec			(0x310)
#define ivMsg_ToStopAudioRec			(0x311)
/*----------------------------------------------------------------------------------
	Notify application to Start/Stop the audio record.
	Strongly recommend that you use this Audio Control mechanism,
	Parameter: NA
	Return: NA
==================================================================================*/

/* Recognize information and result */
#define ivMsg_SpeechStart				(0x401)
/*----------------------------------------------------------------------------------
	Notify application that the speech start Detected, the Speech maybe start 
	probably. As the auto VAD(voice-active-detection) is not Completely accurate, 
	this message may be sent several times in process of one recognizing task.
	Parameter: NA
	Return: NA
==================================================================================*/
#define ivMsg_SpeechEnd					(0x402)
/*----------------------------------------------------------------------------------
	Notify application that the Speech End Detected.
	Parameter: NA
	Return: NA
==================================================================================*/
#define ivMsg_SpeechFlushEnd			(0x403)
/*----------------------------------------------------------------------------------
	Notify application that the Speech ended because EsrEndAudioData called while
	the ESR object had NOT detected speech end, The	Speech start must be Detected
	previously already. If speech end was detected firstly, ivMsg_SpeechEnd will be
	send no matter whether EsrEndAudioData was called.
	Parameter: NA
	Return: NA
==================================================================================*/
#define ivMsg_NoSpeechDetected			(0x40f)
/*----------------------------------------------------------------------------------
	Notify application that the ESR object has NOT detected any speech while 
	the application called EsrEndAudioData.
	Parameter: NA
	Return: NA
==================================================================================*/


#define ivMsg_ResponseTimeout			(0x410)
/*----------------------------------------------------------------------------------
	Notify application that EsrObject can not detect speech start after run over
	the agreed Response-Time,
	Parameter: NA
	Return:
		ivErr_Reset	: Ignore this message and Re-timing.
		Any Other	: The Process was failed and over!
==================================================================================*/
#define ivMsg_SpeechTimeout				(0x411)
/*----------------------------------------------------------------------------------
	Notify application that the speech run over the agreed Time before EsrObject
	produce some result.
	Parameter:
	wParam : [as ivBool] Indicate whether can force produce the recognized result.
	lParam : NA
	return:
		ivErr_OK:	 Try to force produce the recognized result if possible, 
		ivErr_FALSE: The process was failed and over!
==================================================================================*/
#define ivMsg_EndedByUser				(0x412)
#define ivMsg_AudioCUT				    (0x413)
#define ivMsg_AudioNORMAL				(0x414)
#define ivMsg_AudioLOWER				(0x415)
#define ivMsg_SpeechTooEarly			(0x416)
/*----------------------------------------------------------------------------------
	Notify application that user ended the process before EsrObject produce some result.
	Parameter: NA
	Return: NA
==================================================================================*/
#define ivMsg_Result					(0x500)
#define ESR_SLOT_TEXT	(1)	/* Command Text Slot */
#define ESR_SLOT_DIGIT	(2)	/* Digit String Slot */
#define ESR_SLOT_VALUE	(3)	/* Numerical Value Slot */
#define ESR_SLOT_FM		(4) /*  */
typedef struct tagWordItem{
	ivUInt32  nID;
	ivCStrW  pText;
}TWordItem, ivPtr PWordItem;
typedef TWordItem ivCPtr PCWordItem;
typedef struct tagSlotInfo{
	ivCharW		szName[40/sizeof(ivChar)];
	ivUInt32	iSlotType;

	ivUInt32	nConfidenceScore; /* Reserved */
	ivUInt32	nItem;
	PCWordItem  pItems;
}TSlotInfo,ivPtr PSlotInfo;
typedef TSlotInfo ivCPtr PCSlotInfo;
typedef struct tagEsrResult{
	ivUInt32	iSyntaxID;			/* Sentence ID */
	ivUInt32	nConfidenceScore;	/* Score of Confidence 1-100 */
	ivUInt32	nSlot;				/* Slot count of this sentence */
	PCSlotInfo	pSlots;				/* Recognize result array of the slot list */
}TEsrResult, ivPtr PEsrResult;
typedef TEsrResult ivCPtr PCEsrResult;
/*----------------------------------------------------------------------------------
	Notify application that EsrObject produced some result or refuse.
	EsrObject output result by this message. When some results were produced,
	the wParam indicate the count of probable result, and lParam point to the
	array of probable result information. The Array is decreasing ordered by the
	member nConfidenceScore.
	However, if the EsrObject detected the speech is over, but can NOT produce
	any	result (that is, Refused), This message will still be sent, wParam will
	be zero	and lParam will be ivNull.
	Parameter: 
		wParam : [as ivUInt32] N-Best count
		lParam : [as PCEsrResult] The Array of the result information structures.
				The content of this pointer keep valid in 30000 ms or before next 
				recognize task start.
	return: NA
==================================================================================*/

/* Definition of ESR parameters and parameter value */
/* Parameter ID for ESR Sensitivity */
#define ES_CM_THRESHOLD			(1)
	#define CM_THRESHOLD_DEFAULT			((ivCPointer)0)
 

/* Parameter ID for ESR Response Timeout */
#define ES_PARAM_RESPONSETIMEOUT		(2)
	#define ES_MIN_RESPONSETIMEOUT		((ivCPointer)1000)		/* In milliseconds */
	#define ES_MAX_RESPONSETIMEOUT		((ivCPointer)5000)
	#define ES_COMMEND_RESPONSETIMEOUT	((ivCPointer)3000)		/* In milliseconds */
	#define ES_DEFAULT_RESPONSETIMEOUT	ES_COMMEND_RESPONSETIMEOUT
	#define ESR_INFINIT					((ivCPointer)0) /* infinite */


/* Parameter ID for ESR Speech Timeout */
#define ES_PARAM_SPEECHTIMEOUT			(3)
	#define ES_MIN_SPEECHETIMEOUT		((ivCPointer)1000)		/* In milliseconds */
	#define ES_MAX_SPEECHETIMEOUT		((ivCPointer)20000)
	#define ES_COMMEND_SPEECHETIMEOUT	((ivCPointer)4000)		/* In milliseconds */
	#define ES_DEFAULT_SPEECHETIMEOUT	ES_COMMEND_SPEECHETIMEOUT

/* Parameter ID for ESR speech detected notify or not */
#define ES_PARAM_SPEECHNOTIFY			(4)
	#define ES_SPEECHNOTIFY_ON			((ivCPointer)ivTrue)	
	#define ES_SPEECHNOTIFY_OFF			((ivCPointer)ivFalse)	
	#define ES_DEFAULT_SPEECHNOTIFY		ES_SPEECHNOTIFY_ON		/* No speech start notify */

/* Parameter ID for dictate How many initial millisecond
	will be Discarded after the audio-rec device be turned on */
#define ES_PARAM_AUDIODISCARD			(5)
	#define ES_DEFAULT_AUDIODISCARD		(0) 

/* Parameter ID for ESR Disable VAD */
#define ES_PARAM_VAD				(6)
	#define ES_VAD_ON			((ivCPointer)1)
	#define ES_VAD_OFF			((ivCPointer)0)
	#define ES_DEFAULT_VAD		ES_VAD_ON

/* Parameter ID for ESR Disable CM */
#define ES_PARAM_CM				(7)
#define ES_CM_ON			((ivCPointer)1)
#define ES_CM_OFF			((ivCPointer)0)
#define ES_DEFAULT_CM		ES_CM_ON 

/* Parameter ID for ESR Disable AGC */
#define ES_PARAM_DISABLEAGC				(8)
#define ES_DISABLEAGC_ON			((ivCPointer)1)
#define ES_DISABLEAGC_OFF			((ivCPointer)0)
#define ES_DEFAULT_DISABLEAGC		ES_DISABLEAGC_ON

/* Parameter ID for ESR Disable SPEECHTOOEARLY */
#define ES_PARAM_DISABLE_SPEECHTOOEARLY				(9)
#define ES_DISABLE_SPEECHTOOEARLY_ON			((ivCPointer)1)
#define ES_DISABLE_SPEECHTOOEARLY_OFF			((ivCPointer)0)
#define ES_DEFAULT_DISABLE_SPEECHTOOEARLY		ES_DISABLE_SPEECHTOOEARLY_ON 

/* Parameter ID for gauss selection */
#define ES_PARAM_GS				(10)
#define ES_GS_ON				((ivCPointer)1)
#define ES_GS_OFF				((ivCPointer)0)
#define ES_DEFAULT_GS			((ivCPointer)0)

/* Parameter ID for two pass */
#define ES_PARAM_TWOPASS		(11)
#define ES_TWOPASS_ON			((ivCPointer)1)
#define ES_TWOPASS_OFF			((ivCPointer)0)
#define ES_DEFUALT_TWOPASS		((ivCPointer)1)

/* Parameter ID for prune threshold */
#define ES_PARAM_PRUNE					(12)
#define ES_DEFAULT_PRUNE				((ivCPointer)350)

#define ivESR_CP_GBK					936		/* GBK (default) */
#define ivESR_CP_BIG5					950		/* Big5 */
#define ivESR_CP_UTF16LE				1200	/* UTF-16 little-endian */
#define ivESR_CP_UTF16BE				1201	/* UTF-16 big-endian */
#define ivESR_CP_UTF8					65001	/* UTF-8 */
#define ivESR_CP_GB18030				ivESR_CP_GBK
#define ivESR_CP_UNICODE				ivESR_CP_UTF16LE
#define ivESR_CP_UNICODEBE				ivESR_CP_UTF16BE
#define ivESR_CP_DEFAULT				ivESR_CP_UTF16LE

/*
*	Interface
*/
#define  EsrCreate								 AIT31234789gh2asdfjeisdkfjieusdhui2h
#define  EsrDestroy								 AIT31234789gh2asdfje1234568d6ieusd2h
#define	 EsrBuildGrammar						 AIT3A974AF0A516A45d2B331A791741C780B
#define  EsrLexiconInsertItem					 AIT33F4DD4E81837466b8EA763F1CC3D576E
#define  EsrLexiconDeleteItem					 AIT30A4F7C5AE7F747559F3A058FF10D5CAC
#define  EsrLexiconUpdate                        AIT318A21BE01B54d7a83B6A087786A3F7CF
#define  EsrConvertAToU							 AIT33DB091F2EC5C46b5BCC89969EFA0208C
#define  EsrAppendAudioData						 AIT3D3055C9368DE48229F61A8802CE608DB
#define  EsrEndAudioData						 AIT3C6D35D35D5B747baB180801D90191185
#define  EsrRunServiceStep						 AIT3AF529CAD4C654dff8DBFBFB34A197C5D
#define  EsrRunService							 AIT316AC859CDA5145e286D104C4B8738CAD
#define  EsrExitService							 AIT3B3711195371F496bAB02230F44489B95
#define  EsrRecognize							 AIT3670EECAADAA54490A71F939986A840C1
#define  EsrStart								 AIT300494118753C43628F7E5B2D0918239D
#define  EsrStop								 AIT312347s74cuywdh62gsjdibbc4568d6ie
#define  EsrMakeVoicetag						 AIT31F32ED2E869343aa891E1152B8D24C6D
#define  EsrGetUserData							 AIT359B3A3D2437C43509CD580B6DAB38A2A
#define  EsrSetUserData							 AIT3AF0C0170F74A4e1b949828006D18F095
#define  EsrGetErrInfo							 AIT3C155FB5F1F344346A05322F9E7D46B1A
#define  EsrSetParam							 AIT31234789gasdoieyr8asdf1234568d6ie
#define  EsrGetVersion							 AIT31234789gasdfiwo38asdfbbc4568d6ie
#define	 EsrLexiconCreate						 AIT3686B763B77CC41d88E400FA889D4556E
#define  EsrLexiconUnload                         AIT312B61CB8943C4931B66D4CC7FDFB6FAA

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

/* Create ESR object */
ivStatus								/* Returned Error Info */
ivCall EsrCreate(
		ivHandle ivPtr	phEsrObj,		/* [Out] To Receive the ESR object handle */
		ivPCUserOS		pUserOS			/* [In] Describe the user's OS and Interface */
	);

/* Destroy ESR object */
void 
ivCall EsrDestroy(
		ivHandle		hEsrObj			/* The ESR object handle */
	);

/* Build Grammar from text */
ivStatus								/* Returned Error Info */
ivCall EsrBuildGrammar(
		ivHandle		hEsrObj,		/* The ESR object handle */
     	ivPCByte		lpText,			/* A bnf-format text buffer*/
		ivSize			len				/* the length of the buffer */
	);


ivStatus 
ivCall EsrConvertAToU(
		ivHandle hEsrObj, 
		ivPInt16 pBuffer, 
		ivPUInt8 pnBufferSize, 
		ivCStrA pStrA, 
		ivUInt16 nEncoding);

/* 重新创建一个词典 */
ivStatus
ivCall EsrLexiconCreate(
						  ivHandle	hEsrObj,			/* The ESR object handle */
						  ivCStrW	lpLexicon			/* The lexicon's name */
						);

/* Add One command text,Call this function Repeatedly to make a lexicon */
ivStatus								/* Returned Error Info */
ivCall EsrLexiconInsertItem(
                          ivHandle		hEsrObj,		/* The ESR object handle */
                          ivCStrW		lpLexicon,		/* The lexicon's name */
                          ivCStrW		lpContent,		/* The command word's text content */
                          ivUInt32		nID				/* A associated ID, ESR will use this ID 
                                                        to notice the recognization result */
                                                        );

/* Delete One command text, */
ivStatus								/* Returned Error Info */
ivCall EsrLexiconDeleteItem(
                          ivHandle		hEsrObj,		/* The ESR object handle */
                          ivCStrW		lpLexicon,		/* The lexicon's name */
                          ivCStrW		lpContent		/* The command word's text content */
                                                         );

/* Update the lexicon */
ivStatus								/* Returned Error Info */
ivCall EsrLexiconUpdate(
                           ivHandle		hEsrObj,		/* The ESR object handle */
                           ivCStrW		lpLexicon		/* The lexicon's name */
                            );

/* Unload the lexicon */
ivStatus								/* Returned Error Info */
ivCall EsrLexiconUnload(
							ivHandle		hEsrObj,		/* The ESR object handle */
							ivCStrW			lpLexicon		/* The lexicon's name */
						);


/* Append Audio data to the ESR object,In general, Call this function in record thread */
ivStatus								/* Returned Error Info */
ivCall EsrAppendAudioData(
		ivHandle		hEsrObj,		/* The ESR object handle */
		ivPInt16	 	pAudioData,		/* [In] Input Audio data buffer */
		ivSize			nSamples		/* Specifies the length, in samples, of Audio data */
	);

/* Tell ESR object that there's no more data, If you control record end Timing
   by yourself, Call this function after stop audio record is recommended. */
ivStatus								/* Returned Error Info */
ivCall EsrEndAudioData(
		ivHandle		hEsrObj			/* The ESR object handle */
	);

/* Call ESR to do piece of operation 
   Generally, this function is called on a timer */
ivStatus								/* Returned Error Info */
ivCall EsrRunServiceStep(
		ivHandle		hEsrObj			/* The ESR object handle */
	);


/* Do all the operation, It Occupies a thread exclusively */
ivStatus								/* Returned Error Info */
ivCall EsrRunService(
		ivHandle		hEsrObj		/* The ESR object handle */
	);

/* Notify the run Service to terminate when finished current recognize 
   process (if there is), The ESR object must be in run-Service State 
   That is, EsrRunService must be called previously */
ivStatus								/* Returned Error Info */
ivCall EsrExitService(
		ivHandle	hEsrObj			/* The ESR object handle */
	);

/* Recognize synchronous */
ivStatus								/* Returned Error Info */
ivCall EsrRecognize(
		ivHandle		hEsrObj,		/* The ESR object handle */
		ivStr			lpGrmrName,		/* Name of grammar, Grammar must be completely created previously */
		ivPInt16	 	pAudioData,		/* [In] Input Audio data buffer */
		ivSize			nSamples,		/* Specifies the length, in samples, of Audio data */
		ivPUInt32		pnBest,			/* [Out] To Receive Recognize result nBest Count */
		PCEsrResult ivPtr ppResult		/* [Out] To Receive Recognize result */
	);

/* Start one process of recognition */
ivStatus								/* Returned Error Info */
ivCall EsrStart(
		ivHandle		hEsrObj,		/* The ESR object handle */
		ivStr			lpGrmrName		/* Name of grammar, Grammar must be completely created previously */
	);

/* Stop one process of recognition */
ivStatus								/* Returned Error Info */
ivCall EsrStop(ivHandle hEsrObj);		/* The ESR object handle */

/* Recognize synchronous */
ivStatus								/* Returned Error Info */
ivCall EsrMakeVoicetag(
		ivHandle		hEsrObj,		/* The ESR object handle */
		ivStr			lpLexName,		/* Name of the lexicon using Voicetag */
		ivStrW			lpVTName,		/* Name of Voicetag */		
		ivPInt16	 	pAudioData,		/* [In] Input Audio data buffer */
		ivSize			nSamples		/* Specifies the length, in samples, of Audio data */
	);

/* Set ESR object parameter */
ivStatus								/* Returned Error Info */
ivCall EsrSetParam(
		ivHandle		hEsrObj,		/* The ESR object handle */
		ivUInt32		nParamID,		/* Parameter ID */
		ivCPointer 		nParamValue		/* Parameter Value */
	);

/* Get/Set user data binded with ESR object */
ivPointer								/* User data binded with ESR object */
ivCall EsrGetUserData(
		ivHandle		hEsrObj			/* The ESR object handle */
	);
void 
ivCall EsrSetUserData(
		ivHandle		hEsrObj,		/* The ESR object handle */
		ivPointer		pUser			/* User data to bind */
	);

/* Get the string description of the latest ivStatus ErrorCode */
ivCStrA									/* Error information string */
ivCall EsrGetErrInfo(
		ivStatus		nErrCode		/* Error Code */
	);

/* Get SDK Version */
ivStatus								/* Returned Error Info */
ivCall EsrGetVersion(
		ivPUInt8		piMajor,		/* [Out] Main version */
		ivPUInt8		piMinor		/* [Out] Sub-version */
	);

#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif /* !defined(ESR_TEAM__2007_08_08__ESR__H) */
