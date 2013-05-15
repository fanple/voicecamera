/*----------------------------------------------+
|												|
|	ivErrorCode.h - Basic Definitions		|
|												|
|		Copyright (c) 1999-2007, iFLYTEK Ltd.	|
|		All rights reserved.					|
|												|
+----------------------------------------------*/

#ifndef IFLYTEK_VOICE__2008_10_13_ERRORCODE__H
#define IFLYTEK_VOICE__2008_10_13_ERRORCODE__H

#include "ivEsrDefine.h"

#ifdef __cplusplus
extern "C" {
#endif

#if IV_FIXED_ERRORCODE
/* For license check */
#define ivErr_InvSN				((ivStatus)2)

/* General */
#define ivErr_InvArg			((ivStatus)3)
#define ivErr_BufferFull		((ivStatus)4)
#define ivErr_Failed			((ivStatus)5)
#define ivErr_NotSupport		((ivStatus)6)
#define ivErr_OutOfMemory		((ivStatus)7)
#define ivErr_InvResource		((ivStatus)8)
#define ivErr_NotFound			((ivStatus)9)
#define ivErr_InvGrmr           ((ivStatus)10)

/* For object status */
#define ivErr_InvCall			((ivStatus)11)

/* For ASR Input */
#define ivErr_SyntaxError		((ivStatus)12)

/* For Message Call Back */
#define ivErr_Reset				((ivStatus)13)

#define ivErr_Ended				((ivStatus)14)
#define ivErr_Idle				((ivStatus)15)

#define ivErr_CanNotSaveFile    ((ivStatus)16)

/* For Lexicon name */
#define ivErr_InvName    ((ivStatus)17)

#define ivErr_BufferEmpty		((ivStatus)18)

#define ivErr_GetResult			((ivStatus)19)

#define ivErr_ReactOutTime			((ivStatus)20)		/*反应超时*/
#define ivErr_SpeechOutTime			((ivStatus)21)		/*语音超时*/

#define ivErr_CUT     ((ivStatus)22)                    /*录音质量过高*/
#define ivErr_LOWER     ((ivStatus)23)                   /*录音质量过小*/
#define ivErr_Limitted  ((ivStatus)24)                   /*授权不够*/

#define ivErr_GrmSelfIdentifyingInvalidFormat               0x1001
#define ivErr_GrmSelfIdentifyingMissingBnf                  0x1002
#define ivErr_GrmSelfIdentifyingMissingBlank                0x1003
#define ivErr_GrmSelfIdentifyingInvalidVer                  0x1004 

#define ivErr_GrmEncodeMissingBom                           0x1005
#define ivErr_GrmEncodeUnsupport                            0x1006
#define ivErr_GrmEncodeBomConflict                          0x1007

#define ivErr_GrmHdrUnknowDefinition                        0x1008
#define ivErr_GrmHdrStartRedefine                           0x1009
#define ivErr_GrmHdrGrammarNameRedefine                     0x100a

#define ivErr_GrmBnfEmptyInput                              0x100b
#define ivErr_GrmBnfInvalidInput                            0x100c

#define ivErr_GrmRuleNameInvalid                            0x100d
#define ivErr_GrmRuleDefinitionDup                          0x100e
#define ivErr_GrmRuleMissingAssign                          0x100f
#define ivErr_GrmRuleMissingSubexpansion                    0x1010
#define ivErr_GrmRuleMissingSemecolon                       0x1011
#define ivErr_GrmRuleMissingName                            0x1012
#define ivErr_GrmRuleNotExist                               0x1013


#define ivErr_GrmParenEmpty                                 0x1014
#define ivErr_GrmParenDismatch                              0x1015

#define ivErr_GrmBracketEmpty                               0x1016
#define ivErr_GrmBracketDismatch                            0x1017

#define ivErr_GrmSlotNameInvalid                            0x1018
#define ivErr_GrmSlotNotExist                               0x1019
#define ivErr_GrmSlotTooComplex                             0x101a

#define ivErr_GrmIDMissingLeftParen                         0x101b
#define ivErr_GrmIDInvalid                                  0x101c
#define ivErr_GrmIDMissingRightParen                        0x101d

#define ivErr_GrmHdrLanguageRedefine                        0x101e
#define ivErr_GrmHdrMissingStart                            0x101f
#define ivErr_GrmHdrMissingGrammarName                      0x1020
#define ivErr_GrmHdrMissingSemi                             0x1021
#define ivErr_GrmHdrStartRuleNotExist                       0x1022
#define ivErr_GrmHdrGrammarNameInvalid                      0x1023
#define ivErr_GrmHdrSlotNameInvalid                         0x1024
#define ivErr_GrmHdrStartRuleInvalid                        0x1025
#define ivErr_GrmHdrKeywordInvalid                          0x1026

#define ivErr_GrmKeywordInvalid                             0x1027

#define ivErr_GrmStreamInvalidNetFile                       0x1028
#define ivErr_GrmStreamInvalidLexFile                       0x1029

#define ivErr_GrmSyntaxRecursion                            0x102a

#define ivErr_GrmTokenInvalid                               0x102b
#define ivErr_GrmTokenTooLong                               0x102c
#define ivErr_GrmTokenPronNotFound                          0x102d
#define ivErr_GrmTokenInvalidBetweenQuote                   0x102e
#define ivErr_GrmItemNotFound                               0x102f

#define ivErr_GrmLexNotExist                                0x1030
#define ivErr_GrmLexEmpty                                   0x1031
#define ivErr_GrmLexiconAlreadyExist                        0x1032
#define ivErr_GrmLexUpdateError                             0x1033


#define ivErr_GrmRepeatMissingLeftParen                     0x1034
#define ivErr_GrmRepeatMissingRigthParen                    0x1035
#define ivErr_GrmRepeatEmpty                                0x1036
#define ivErr_GrmRepeatEndless                              0x1037



#define ES_DEFINE_ERROR(e)		ivTextA(#e),
ivExtern ivConst ivCStrA g_szErrorInfo[];

#else
#define ES_DECLARE_ERROR(e)			ivExtern ivConst ivChar e[];
#if IV_UNICODE 
	#define ES_DEFINE_ERROR(e)		ivConst ivChar e[] = L###e;
#else
	#define ES_DEFINE_ERROR(e)		ivConst ivChar e[] = #e;
#endif

/* For license check */
ES_DECLARE_ERROR(ivErr_InvSN)

/* General */
ES_DECLARE_ERROR(ivErr_InvArg)
ES_DECLARE_ERROR(ivErr_BufferFull)
ES_DECLARE_ERROR(ivErr_Failed)
ES_DECLARE_ERROR(ivErr_NotSupport)
ES_DECLARE_ERROR(ivErr_OutOfMemory)
ES_DECLARE_ERROR(ivErr_InvResource)
ES_DECLARE_ERROR(ivErr_NotFound)
ES_DECLARE_ERROR(ivErr_InvGrmr)

/* For object status */
ES_DECLARE_ERROR(ivErr_InvCall)

/* For ASR Input */
ES_DECLARE_ERROR(ivErr_SyntaxError)

/* For Message Call Back */
ES_DECLARE_ERROR(ivErr_Reset)

ES_DECLARE_ERROR(ivErr_Ended)
ES_DECLARE_ERROR(ivErr_Idle)

ES_DECLARE_ERROR(ivErr_CanNotSaveFile)

ES_DECLARE_ERROR(ivErr_InvName)

#endif /* IV_FIXED_ERRORCODE */

ivCStrA ivGetErrInfo(ivStatus e);

#ifdef __cplusplus
}
#endif


#define	ivErr_OK				((ivStatus)0)
#define	ivErr_FALSE				((ivStatus)1)

#define ivSucceeded(hr)			((ivUInt32)(hr) <= 1)

#endif /* !IFLYTEK_VOICE__2008_10_13_ERRORCODE__H */
