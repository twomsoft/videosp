
// FastPlay 音视频引擎 (For Linux)
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

// 发送器
struct IVideoSender 
{
	// 开始
	virtual HRESULT Start(const char* url) = 0;

	// 停止
	virtual void End() = 0;

	// 设置预览窗口
	virtual void SetPreview(HWND hwnd) = 0;

	// 设置视频采集模式
	enum VIDEO_MODE {
		VideoModeDisable,	// 没有视频采集
		VideoModeCamera,	// 采集摄像头
		VideoModeScreen,	// 采集屏幕
		VideoModeScreenWithCamera	// 采集屏幕但同时开启摄像头预览
	};
	virtual void SetVideoMode(int mode) = 0;

	// 视频/音频 开关
	enum ENABLE_FLAG {
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
	virtual HRESULT Load(const char* url, int flag) = 0;

	enum PLAY_FLAG {
		FlagAudio		=1u,		// 音频
		FlagVideoLow	=(1u<<1),	// 低清视频 
		FlagVideoHigh	=(1u<<2)	// 高清视频
	};
	virtual int GetFlag() = 0;
	virtual void SetFlag(int flag) = 0;

	// 返回状态
	virtual int GetStatus() = 0;
	enum STATUS {
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

// 设备接口
struct IDeviceManager {

	virtual const char* CameraName(int id) = 0; // 返回摄像头名称，如果返回NULL表示无此设备

	virtual void SelectCamera(int id) = 0;	// 选择摄像头
};

// 音视频接口
struct IFastPlay
{
	virtual void Init(const char* user=NULL) = 0;

	// 清理 (程序退出时)
	virtual void Uninit() = 0;

	// 返回版本号
	virtual uint32_t Ver() = 0;

	// 返回设备接口
	virtual IDeviceManager* GetDeviceManager() = 0;

	// 返回发送器
	virtual IVideoSender* GetSender() = 0;

	// 创建播放器
	virtual IVideoPlayer* CreatePlayer(HWND hwnd) = 0;
};

#ifdef _WIN32
#define PLAY_API
#else
#define PLAY_API __attribute__((visibility("default")))	
#endif

PLAY_API IFastPlay* GetFastPlay();
