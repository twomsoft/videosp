
// FastPlay ����Ƶ���� (For Linux)
// 
#pragma once

#ifdef _WIN32
#include <Windows.h>
#else
#include <X11/Xlib.h>
typedef int HRESULT;
typedef Window HWND;
#define S_OK (0)
#endif

#include <inttypes.h>

// ������
struct IVideoSender 
{
	// ��ʼ
	virtual HRESULT Start(const char* url) = 0;

	// ֹͣ
	virtual void End() = 0;

	// ����Ԥ������
	virtual void SetPreview(HWND hwnd) = 0;

	// ������Ƶ�ɼ�ģʽ
	enum VIDEO_MODE {
		VideoModeDisable,	// û����Ƶ�ɼ�
		VideoModeCamera,	// �ɼ�����ͷ
		VideoModeScreen,	// �ɼ���Ļ
		VideoModeScreenWithCamera	// �ɼ���Ļ��ͬʱ��������ͷԤ��
	};
	virtual void SetVideoMode(int mode) = 0;

	// ��Ƶ/��Ƶ ����
	enum ENABLE_FLAG {
		FlagAudio =1u,
		FlagVideo =2u
	};
	virtual void SetFlags(uint32_t f) = 0;
	virtual uint32_t GetFlags(void) = 0;
};

// ������
struct IVideoPlayer {
	virtual void Destroy() = 0;

	// ���� / ж��
	virtual HRESULT Load(const char* url, int flag) = 0;

	enum PLAY_FLAG {
		FlagAudio		=1u,		// ��Ƶ
		FlagVideoLow	=(1u<<1),	// ������Ƶ 
		FlagVideoHigh	=(1u<<2)	// ������Ƶ
	};
	virtual int GetFlag() = 0;
	virtual void SetFlag(int flag) = 0;

	// ����״̬
	virtual int GetStatus() = 0;
	enum STATUS {
		StatusClosed,
		StatusLoading,
		StatusPlaying,
		StatusError,
	};

	// �������� [0-100]
	virtual void SetVolume(int vol) = 0;

	// ��������
	virtual int GetVolume() = 0;
};

// �豸�ӿ�
struct IDeviceManager {

	virtual const char* CameraName(int id) = 0; // ��������ͷ���ƣ��������NULL��ʾ�޴��豸

	virtual void SelectCamera(int id) = 0;	// ѡ������ͷ
};

// ����Ƶ�ӿ�
struct IFastPlay
{
	virtual void Init(const char* user=NULL) = 0;

	// ���� (�����˳�ʱ)
	virtual void Uninit() = 0;

	// ���ذ汾��
	virtual uint32_t Ver() = 0;

	// �����豸�ӿ�
	virtual IDeviceManager* GetDeviceManager() = 0;

	// ���ط�����
	virtual IVideoSender* GetSender() = 0;

	// ����������
	virtual IVideoPlayer* CreatePlayer(HWND hwnd) = 0;
};

#ifdef _WIN32
#define PLAY_API
#else
#define PLAY_API __attribute__((visibility("default")))	
#endif

PLAY_API IFastPlay* GetFastPlay();
