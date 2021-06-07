
#pragma once

#define WIN32_LEAN_AND_MEAN 
#include <windows.h>
#include <mmsystem.h>
#include <inttypes.h>
#include <Ole2.h>

#ifdef FASTPLAY_EXPORTS
#define FASTPLAY_API extern "C" __declspec(dllexport)
#else
#define FASTPLAY_API extern "C" __declspec(dllimport)
#endif

// �豸����
struct IDevices {

	enum TYPE {
		TYPE_SPEAKER,	// ������
		TYPE_MIC,		// ��˷�
		TYPE_CAMERA,	// ����ͷ
	};
	enum CONFIG {
		MAX_ITEMS =10U
	};

	struct ITEM {
		BSTR name;
		BSTR id; // OR PATH

		ITEM() :name(NULL), id(NULL)
		{
		}
		void clear() {
			SysFreeString(id); id = NULL;
			SysFreeString(name); name = NULL;
		}
	};
	struct LIST {
		TYPE type;				// [in]
		int count;				// [out]
		int current;			// [out] ��ǰ�豸����
		ITEM items[MAX_ITEMS];	// [out]
	};
	virtual HRESULT GetList(LIST*) = 0;

	// ѡ���豸 (NULL��ʾĬ��)
	virtual HRESULT SetDevice(TYPE type, const wchar_t id[]) = 0;

	// ��������ͷ������
	// def: 480(Ĭ��) / 720 / 1080
	virtual HRESULT SetCameraDef(const uint32_t def) = 0;
};

// ��������
struct IVideoSender {
	// ��ʼ
	virtual HRESULT Start(const wchar_t* url) = 0;

	// ֹͣ
	virtual void End() = 0;

	// ����Ԥ������
	virtual void SetPreview(HWND hwnd) = 0;

	// ������Ƶ�ɼ�ģʽ
	enum VideoMode {
		VideoModeDisable,	// û����Ƶ�ɼ�
		VideoModeCamera,	// �ɼ�����ͷ
		VideoModeScreen,	// �ɼ���Ļ
		VideoModeScreenWithCamera	// �ɼ���Ļ��ͬʱ��������ͷԤ��
	};
	virtual void SetVideoMode(int mode) = 0;

	// ��Ƶ/��Ƶ ����
	enum Flag {
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
	virtual HRESULT Load(const wchar_t* url, int flag) = 0;

	enum Flag {
		FlagAudio =1u,			// ��Ƶ
		FlagVideoLow =(1u<<1),	// ������Ƶ 
		FlagVideoHigh =(1u<<2)	// ������Ƶ
	};

	virtual int GetFlag() = 0;

	virtual void SetFlag(int flag) = 0;

	// ����״̬
	virtual int GetStatus() = 0;
	enum Status {
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

// ��������Ƶ�ӿ�
struct IFastPlay 
{
	// ����SDK�û�
	virtual void SetSdkUser(const wchar_t* name) = 0;

	// ���ذ汾��
	virtual uint32_t Ver() = 0;

	// �����豸�������
	virtual IDevices* GetDevices() = 0;

	// ���ط�����
	virtual IVideoSender* GetSender() = 0;

	// ����������
	virtual IVideoPlayer* CreatePlayer(HWND hwnd) = 0;

	// �ڲ�����
	virtual int Test(int op) = 0;
};

FASTPLAY_API IFastPlay* GetFastPlay();

FASTPLAY_API void DebugOut(const char* fmt, ...);
