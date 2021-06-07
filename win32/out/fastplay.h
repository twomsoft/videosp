
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

// 设备管理
struct IDevices {

	enum TYPE {
		TYPE_SPEAKER,	// 扬声器
		TYPE_MIC,		// 麦克风
		TYPE_CAMERA,	// 摄像头
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
		int current;			// [out] 当前设备索引
		ITEM items[MAX_ITEMS];	// [out]
	};
	virtual HRESULT GetList(LIST*) = 0;

	// 选择设备 (NULL表示默认)
	virtual HRESULT SetDevice(TYPE type, const wchar_t id[]) = 0;

	// 设置摄像头清晰度
	// def: 480(默认) / 720 / 1080
	virtual HRESULT SetCameraDef(const uint32_t def) = 0;
};

// 流发送器
struct IVideoSender {
	// 开始
	virtual HRESULT Start(const wchar_t* url) = 0;

	// 停止
	virtual void End() = 0;

	// 设置预览窗口
	virtual void SetPreview(HWND hwnd) = 0;

	// 设置视频采集模式
	enum VideoMode {
		VideoModeDisable,	// 没有视频采集
		VideoModeCamera,	// 采集摄像头
		VideoModeScreen,	// 采集屏幕
		VideoModeScreenWithCamera	// 采集屏幕但同时开启摄像头预览
	};
	virtual void SetVideoMode(int mode) = 0;

	// 视频/音频 开关
	enum Flag {
		FlagAudio =1u,
		FlagVideo =2u
	};
	virtual void SetFlags(uint32_t f) = 0;
	virtual uint32_t GetFlags(void) = 0;
};

// 播放器
struct IVideoPlayer {
	virtual void Destroy() = 0;

	// 载入 / 卸载
	virtual HRESULT Load(const wchar_t* url, int flag) = 0;

	enum Flag {
		FlagAudio =1u,			// 音频
		FlagVideoLow =(1u<<1),	// 低清视频 
		FlagVideoHigh =(1u<<2)	// 高清视频
	};

	virtual int GetFlag() = 0;

	virtual void SetFlag(int flag) = 0;

	// 返回状态
	virtual int GetStatus() = 0;
	enum Status {
		StatusClosed,
		StatusLoading,
		StatusPlaying,
		StatusError,
	};

	// 设置音量 [0-100]
	virtual void SetVolume(int vol) = 0;

	// 返回音量
	virtual int GetVolume() = 0;
};

// 会议音视频接口
struct IFastPlay 
{
	// 设置SDK用户
	virtual void SetSdkUser(const wchar_t* name) = 0;

	// 返回版本号
	virtual uint32_t Ver() = 0;

	// 返回设备管理对象
	virtual IDevices* GetDevices() = 0;

	// 返回发送器
	virtual IVideoSender* GetSender() = 0;

	// 创建播放器
	virtual IVideoPlayer* CreatePlayer(HWND hwnd) = 0;

	// 内部测试
	virtual int Test(int op) = 0;
};

FASTPLAY_API IFastPlay* GetFastPlay();

FASTPLAY_API void DebugOut(const char* fmt, ...);
