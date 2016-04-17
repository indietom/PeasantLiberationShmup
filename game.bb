Const WIN_W=32*15, WIN_H=32*14

Global fullScreen = 2

Graphics WIN_W, WIN_H, 8, fullScreen

SeedRnd MilliSecs()

Global frametimer=CreateTimer(60)
Global starttime=MilliSecs(),elapsedtime,fpscounter,curfps

Function collision(x, y, w, h, x2, y2, w2, h2)
	If y >= y2 + h2 Then Return False 
	If x >= x2 + w2 Then Return False 
	If y + h <= y2 Then Return False
	If x + w <= x2 Then Return False   
 	Return True 
End Function 

Function lerp#(x#, y#, t#)
	Return t# * y# + (1-t#) * x#
End Function 

Function clamp#(v#, min#, max#)
	If v < min Then Return min
	If v > max Then Return max
	Return v
End Function 

Function distanceTo#(x1, y1, x2, y2)
	Return Sqr((x1-x2)^2+(y1-y2)^2)
End Function 

Function frame%(cell, size)
	Return cell*size+1+cell
End Function

Global spritesheet = LoadImage("spritesheet.bmp")
MaskImage(spritesheet, 255, 0, 255)

Type player
	Field x
	Field y
	
	Field imx
	Field imy
	
	Field speed
	
	Field score
	
	Field gunType
	
	Field shadowOffsetX
	Field shadowOffsetY
	
	Field leftKey
	Field rightKey
	Field downKey
	Field upKey
	Field shootKey	
	
	Field destroy
End Type

Function addPlayer(x2, y2)
	p.player = New player
	p\x = x2
	p\y = y2
	
	p\speed = 4
	
	p\imx = 1
	p\imy = 51
	
	p\shadowOffsetX = Cos(-360+45) * 32
	p\shadowOffsetY = Sin(-360+45) * 32
	
	p\leftKey = 203
	p\rightKey = 205
	p\upKey = 200
	p\downKey = 208
	p\shootKey = 57
End Function

Function updatePlayer()
	For p.player = Each player
		If KeyDown(p\leftKey) Then
			p\x = p\x - p\speed
		End If
		
		If KeyDown(p\rightKey) Then
			p\x = p\x + p\speed
		End If
		
		If KeyDown(p\upKey) Then
			p\y = p\y - p\speed
		End If
		
		If KeyDown(p\downKey) Then
			p\y = p\y + p\speed
		End If
		
		If KeyHit(p\shootKey) Then
			If p\gunType = 0 Then
				addProjectile(p\x+12-4, p\y+12-4, -90, p\speed+4, 1, frame(2, 24), 51, 8, 0)
			End If
		End If
		
		If p\destroy Then Delete p
	Next
End Function

Function drawPlayer()
	For p.player = Each player
		DrawImageRect(spritesheet, p\x+p\shadowOffsetX, p\y+p\shadowOffsetY, frame(1, 24), p\imy, 24, 24)
		DrawImageRect(spritesheet, p\x, p\y, p\imx, p\imy, 24, 24)
	Next
End Function

Type projectile
	Field x#
	Field y#
	
	Field speed#
	Field angle#
	
	Field velX#
	Field velY#
	
	Field damage
	
	Field enemy
	
	Field imx
	Field imy
	
	Field size
	
	Field destroy
End Type

Function addProjectile(x2#, y2#, angle2#, speed2#, damage2, imx2, imy2, size2, enemy2)
	p.projectile = New projectile
	p\x = x2
	p\y = y2
	
	p\angle = angle2
	p\speed = speed2
	
	p\damage = damage2
	
	p\imx = imx2
	p\imy = imy2
	
	p\size = size2
	
	p\velX = Cos(p\angle) * p\speed
	p\velY = Sin(p\angle) * p\speed
	
	p\enemy = enemy2
End Function

Function updateProjectile()
	For p.projectile = Each projectile
		p\x = p\x + p\velX
		p\y = p\y + p\velY
		
		If p\destroy Then Delete p
	Next
End Function

Function drawProjectile()
	For p.projectile = Each projectile
		DrawImageRect(spritesheet, p\x, p\y, p\imx, p\imy, p\size, p\size)
	Next
End Function

Const KNIGHT = 0
Const TOWER = 1

Type enemy
	Field x#
	Field y#
	
	Field speed#
	Field angle#
	
	Field velX#
	Field velY#
	
	Field shootAngle#
	Field projectileSpeed#
	
	Field typeOf
	
	Field destroy 
End Type

Function update()
	updatePlayer()
	updateProjectile()
End Function

Function draw()
	drawProjectile()
	drawPlayer()
End Function 

addPlayer(WIN_W/2, WIN_H/2)

While Not KeyHit(1)
	Cls 
		WaitTimer(frametimer)
		Color 102, 255, 119
		Rect 0, 0, WIN_W, WIN_H
		draw()
		update()
		
		If KeyHit(59) Then
			If fullScreen = 2 Then
				fullScreen = 2
			Else
				fullScreen = 2
			End If
			Graphics WIN_W, WIN_H, 8, fullScreen
		End If
	Flip
Wend

FreeImage(spritesheet)