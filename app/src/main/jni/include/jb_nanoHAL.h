#ifndef __JB_NANOGATT__H__
#define __JB_NANOGATT__H__

#ifdef __cplusplus
extern "C" { 
#endif

typedef unsigned char		J_U8;
typedef unsigned short 		J_U16;
typedef unsigned long		J_U32;
typedef char				J_S8;
typedef short				J_S16;
typedef long				J_S32;
typedef float				J_Float;
typedef double				J_Double;
typedef void*				J_Ptr;
typedef int					J_Int;
typedef unsigned int		J_UInt;
typedef long int			J_Size;
typedef J_U8				J_BOOL;

typedef enum{
	CLASS_BLUETOOTH,
	CLASS_DONGLE,
	CLASS_UNKOWN,
}EM_DEVTYPE;

typedef J_Int (*AppCallback) (J_U8* data,J_U32 datalen);
typedef void (*RCNotificationCB)(EM_DEVTYPE dev_type);
typedef void (*VoiceDataCb)(char* pBuf,int length);
typedef void (*VoiceKeyCb)(int action);
typedef void (*WriteCb)(char* pBuf,int length);

/*-----------------------------------------------------------------------------
Function Name:	nano_open
Input		:	
Output		:	
Return 		:
Describe	:
-------------------------------------------------------------------------------*/
int nano_open(AppCallback cb);

/*-----------------------------------------------------------------------------
Function Name:	nano_close
Input		:
Output		:
Return 		:
Describe	:
-------------------------------------------------------------------------------*/
int nano_close(void);

/*-----------------------------------------------------------------------------
Function Name:	nano_appRegister
Input		:
Output		:
Return 		:
Describe	:
-------------------------------------------------------------------------------*/
int nano_appRegister(J_U8* dataIn, J_U8 datalen);

/*-----------------------------------------------------------------------------
Function Name:	nano_appLogin
Input		:
Output		:
Return 		:
Describe	:
-------------------------------------------------------------------------------*/
int nano_appLogin(J_U8* dataIn, J_U8 datalen);

/*-----------------------------------------------------------------------------
Function Name:	nano_appProcData
Input		:
Output		:
Return 		:
Describe	:
-------------------------------------------------------------------------------*/
int nano_appProcData(J_U8* dataIn,J_U8 datalen);

/*-----------------------------------------------------------------------------
Function Name:
Input		:
Output		:
Return 		:
Describe	:
-------------------------------------------------------------------------------*/
void nano_RegisterDataReceivedCb(VoiceDataCb func);
void nano_RegisterVoiceKeyCb(VoiceKeyCb func);
void nano_RegisterWriteCmdCb(WriteCb func);
void nano_RegisterWriteReqCb(WriteCb func);


#ifdef __cplusplus
}
#endif


#endif
