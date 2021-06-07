
#include <gtk/gtk.h>
#include <gdk/gdkx.h>

#include "fastplay.h"

#define print printf

static inline void set_edit_text(GtkWidget* edit, const char* text)
{
	gtk_entry_set_text(GTK_ENTRY(edit), text);
}

static inline const char* get_edit_text(GtkWidget* edit)
{
	return gtk_entry_get_text(GTK_ENTRY(edit));
}

class Dialog 
{
protected:
	GtkWidget* mTopWnd;
	GtkWidget* mLayout;

	static gboolean close_handler(GtkWidget* widget, GdkEvent* event, gpointer data)
	{
		gtk_main_quit();
		return TRUE;
	}
	static void clicked_handler(GtkWidget* widget, gpointer data)
	{
		((Dialog*)data)->on_clicked(widget);
	}
public:
	Dialog(const char* title)
	{
		GtkWidget* w = gtk_window_new(GTK_WINDOW_TOPLEVEL);
		gtk_window_set_title(GTK_WINDOW(w), title);
		gtk_window_set_default_size(GTK_WINDOW(w), 800, 500);
		g_signal_connect(w, "delete_event", G_CALLBACK(close_handler), this);
		mTopWnd = w;
		mLayout = gtk_fixed_new();
		gtk_container_add(GTK_CONTAINER(mTopWnd), mLayout);
	}
	virtual ~Dialog()
	{
		gtk_widget_destroy(mTopWnd);
	}
	virtual void on_clicked(GtkWidget* widget) =0;
	virtual void on_closed()=0;

	void run()
	{
		gtk_widget_show_all(mTopWnd);
		gtk_main();
		on_closed();
		gtk_widget_hide_all(mTopWnd);
	}
	void move(GtkWidget* ctrl, int x, int y, int cx, int cy)
	{
		gtk_fixed_put(GTK_FIXED(mLayout), ctrl, x, y);
		gtk_widget_set_size_request(ctrl, cx, cy);
	}
	GtkWidget* add_button(const char* title, int x, int y, int cx, int cy)
	{
		GtkWidget* btn = gtk_button_new_with_label(title );
		g_signal_connect(btn, "clicked", G_CALLBACK(clicked_handler), this);
		move(btn, x, y, cx, cy);
		return btn;
	}
	GtkWidget* add_edit(int x, int y, int cx, int cy)
	{
		GtkWidget* edit = gtk_entry_new();
		move(edit, x, y, cx, cy);
		return edit;
	}
	GtkWidget* add_canvas(int x, int y, int cx, int cy)
	{
		GtkWidget* canvas =gtk_drawing_area_new();
		move(canvas, x, y, cx, cy);
		return canvas;
	}
};

class MainWnd :public Dialog
{
	GtkWidget* mSendButton;
	GtkWidget* mRecvButton;
	GtkWidget* mSendUrlEdit;
	GtkWidget* mRecvUrlEdit;

	GtkWidget* mPreviewWnd;		// 摄像头预览区
	GtkWidget* mVideoWnd;		// 远端视频区

	IVideoPlayer* m_player;
	IVideoSender* m_sender;

public:
	MainWnd(): Dialog("VIDEO")
	{
		m_player = nullptr;
		m_sender = nullptr;

		init();
	}
	~MainWnd()
	{
		if (m_player)
			m_player->Destroy();
	}
protected:
	void init()
	{
		mSendUrlEdit =add_edit(10, 50, 300, 32);
		mSendButton =add_button("发送", 320, 50, 60, 32);

		mRecvUrlEdit =add_edit(10, 100, 300, 32);
		mRecvButton =add_button("接收", 320, 100, 60, 32);

		mPreviewWnd =add_canvas(10, 150, 320, 240);
		mVideoWnd =add_canvas(340, 150, 320, 240);

		set_edit_text(mSendUrlEdit, "video://192.168.0.104/1234");
		set_edit_text(mRecvUrlEdit, "video://192.168.0.104/1234");

		// 初始化SDK
		IFastPlay* sdk = GetFastPlay();
		sdk->Init();

		// 获取发送器
		m_sender = sdk->GetSender();

		// 枚举摄像头
		IDeviceManager* dev = sdk->GetDeviceManager();
		for (int i = 0; i < 10; i++) {
			const char* name = dev->CameraName(i);
			if (name) {
				print("camera[%d]=%s\n", i, name);
			}
		}
	}
	void on_clicked(GtkWidget* widget)
	{
		if (widget== mSendButton)
		{		
			// 开始推送
			const char* url = get_edit_text(mSendUrlEdit);
			if (m_sender->Start(url) < 0) {
				return; // 失败
			}

			// 设置预览窗口
			if (!gtk_widget_is_drawable(mPreviewWnd)) 
				return; //预览窗口还没准备好 ？
			Window wnd = GDK_WINDOW_XID(gtk_widget_get_window(mPreviewWnd));
			m_sender->SetPreview(wnd);
		}
		else if(widget ==mRecvButton) 
		{
			if (!m_player) {
			// 窗口播放器
				if (!gtk_widget_is_drawable(mVideoWnd))
					return; 

				Window wnd = GDK_WINDOW_XID(gtk_widget_get_window(mVideoWnd));
				m_player = GetFastPlay()->CreatePlayer( wnd );
			}

			const char* url = get_edit_text(mRecvUrlEdit);
			int error = m_player->Load(url,
				IVideoPlayer::FlagAudio | IVideoPlayer::FlagVideoHigh);
			if (error < 0)
			{
				print("error: %d\n", error);
			}
		}
	}
	void on_closed()
	{
		// 清理SDK
		GetFastPlay()->Uninit();

		print("on closed\n");
	}
};

int main(int argc, char* argv[])
{
	XInitThreads();
	gtk_init(&argc, &argv);
	MainWnd mainWnd;
	mainWnd.run();
	print("exit app\n");
	return 0;
}
