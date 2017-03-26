const electron = require('electron')
const {app,BrowserWindow} = electron

app.on('ready', () => {
	let win = new BrowserWindow({width:800, height:710})
	win.loadURL(`file://${__dirname}/LoginForm.html`)
	//win.webContents.openDevTools()
})

exports.openNewWindow = () => {
  let win = new BrowserWindow({width:600, height:400})
  win.loadURL(`file://${__dirname}/index.html`)
}
