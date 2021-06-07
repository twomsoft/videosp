
// demoDlg.cpp: 实现文件
//

#include "pch.h"
#include "framework.h"
#include "demo.h"
#include "demoDlg.h"
#include "afxdialogex.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

IVideoSender* sender = NULL;

IVideoPlayer* player = NULL;
IVideoPlayer* player2 = NULL;

// 用于应用程序“关于”菜单项的 CAboutDlg 对话框

class CAboutDlg : public CDialogEx
{
public:
	CAboutDlg();

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_ABOUTBOX };
#endif

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

// 实现
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialogEx(IDD_ABOUTBOX)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialogEx)
END_MESSAGE_MAP()


// CdemoDlg 对话框



CdemoDlg::CdemoDlg(CWnd* pParent /*=NULL*/)
	: CDialogEx(IDD_DEMO_DIALOG, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CdemoDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CdemoDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDOK, &CdemoDlg::OnBnClickedOk)
	ON_WM_DESTROY()
	ON_BN_CLICKED(IDC_BUTTON1, &CdemoDlg::OnBnClickedButton1)
	ON_BN_CLICKED(IDC_BUTTON2, &CdemoDlg::OnBnClickedButton2)
	ON_BN_CLICKED(IDC_BUTTON3, &CdemoDlg::OnBnClickedButton3)
	ON_BN_CLICKED(IDC_BUTTON4, &CdemoDlg::OnBnClickedButton4)
END_MESSAGE_MAP()


// CdemoDlg 消息处理程序

BOOL CdemoDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// 将“关于...”菜单项添加到系统菜单中。

	// IDM_ABOUTBOX 必须在系统命令范围内。
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// 设置此对话框的图标。  当应用程序主窗口不是对话框时，框架将自动
	//  执行此操作
	SetIcon(m_hIcon, TRUE);			// 设置大图标
	SetIcon(m_hIcon, FALSE);		// 设置小图标

	// TODO: 在此添加额外的初始化代码

	IFastPlay* av = GetFastPlay();

	// 枚举摄像头
	IDevices::LIST list;
	list.type = IDevices::TYPE_CAMERA;
	HRESULT hr =av->GetDevices()->GetList(&list);
	for (int i = 0;i < list.count;i++) {
		list.items[i].clear();
	}
	av->GetDevices()->SetCameraDef(720);

	// 枚举麦克风
	list.type = IDevices::TYPE_MIC;
	hr = av->GetDevices()->GetList(&list);

	// 枚举扬声器
	list.type = IDevices::TYPE_SPEAKER;
	hr = av->GetDevices()->GetList(&list);

	// 创建推送器
	sender = av->GetSender();
	sender->SetPreview(GetDlgItem(IDC_STATIC_VIDEO0)->GetSafeHwnd());
//	sender->SetVideoMode(IVideoSender::VideoModeScreen);

	// 创建播放器
	player = av->CreatePlayer(GetDlgItem(IDC_STATIC_VIDEO1)->GetSafeHwnd());
	player2 = av->CreatePlayer(GetDlgItem(IDC_STATIC_VIDEO2)->GetSafeHwnd());

	SetDlgItemText(IDC_EDIT1, L"video://localhost/test");
	SetDlgItemText(IDC_EDIT2, L"video://localhost/test");
	SetDlgItemText(IDC_EDIT3, L"video://localhost/test");

	return TRUE;  // 除非将焦点设置到控件，否则返回 TRUE
}

void CdemoDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

// 如果向对话框添加最小化按钮，则需要下面的代码
//  来绘制该图标。  对于使用文档/视图模型的 MFC 应用程序，
//  这将由框架自动完成。

void CdemoDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // 用于绘制的设备上下文

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// 使图标在工作区矩形中居中
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// 绘制图标
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialogEx::OnPaint();
	}
}

//当用户拖动最小化窗口时系统调用此函数取得光标
//显示。
HCURSOR CdemoDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}



void CdemoDlg::OnBnClickedOk()
{
	// TODO: 在此添加控件通知处理程序代码
//	CDialogEx::OnOK();
}


void CdemoDlg::OnDestroy()
{
	CDialogEx::OnDestroy();

	// TODO: 在此处添加消息处理程序代码
	
	// 关闭推送和播放
	sender->SetPreview(NULL);
	sender->End();

	player->Destroy();
	player = NULL;

	player2->Destroy();
	player2 = NULL;

	// 停止设备
	GetFastPlay()->Test(0);
}


void CdemoDlg::OnBnClickedButton1()
{
	// 开始推送

	CString editText;
	GetDlgItemText(IDC_EDIT1, editText);

	HRESULT hr = sender->Start(editText);
	DebugOut("result: %d\n", hr);
}


void CdemoDlg::OnBnClickedButton2()
{
	// 高清播放

	CString editText;
	GetDlgItemText(IDC_EDIT2, editText);

	HRESULT hr =player->Load(editText, IVideoPlayer::FlagAudio|IVideoPlayer::FlagVideoHigh);
	DebugOut("result: %d\n", hr);
}


void CdemoDlg::OnBnClickedButton3()
{
	// 低清播放

	CString editText;
	GetDlgItemText(IDC_EDIT3, editText);

	HRESULT hr = player2->Load(editText, IVideoPlayer::FlagVideoLow);
	DebugOut("result: %d\n", hr);
}


void CdemoDlg::OnBnClickedButton4()
{
	// TODO: 在此添加控件通知处理程序代码
	int f =player->GetFlag();
	if (f & IVideoPlayer::FlagVideoHigh)
		f = IVideoPlayer::FlagAudio | IVideoPlayer::FlagVideoLow;
	else
		f = IVideoPlayer::FlagAudio | IVideoPlayer::FlagVideoHigh;
	player->SetFlag(f);
}
