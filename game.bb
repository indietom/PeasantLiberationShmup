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

Function AngleToDirection(angle, aim)
	If angle < -360 And aim = 0 Then angle = angle + 360
	
	If aim Then angle = angle - 180
	
 	Return (angle / 45)*-1
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

Function getAngleToPlayer#(x2#, y2#)
	Local px# 
	Local py# 
	
	For p.player = Each player
		px# = p\x + 16
		py# = p\y + 16
	Next
	
	Return ATan2(py-y2, px-x2)
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
		
		If p\x < -p\size Or p\x >= WIN_W Or p\y < -p\size Or p\y > WIN_H Then p\destroy = 1 
		
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
	
	Field fireRate
	Field maxFireRate
	Field shoot
	Field attacking
	
	Field typeOf
	
	Field dead
	Field deadImx
	Field deadImy
	Field health
	
	Field hitCount
	
	Field hitBoxX
	Field hitBoxY
	Field hitBoxWidth
	Field hitBoxHeight
	
	Field imx
	Field imy
	Field width
	Field height
	
	Field destroy 
End Type

Function addEnemy(x2#, y2#, typeOf2)
	e.enemy = New enemy
	e\x = x2
	e\y = y2
	
	e\typeOf = typeOf2
	
	e\attacking = 1
	
	If e\typeOf = KNIGHT Then
		e\health = 3
		
		e\maxFireRate = 32
		
		e\imx = 1
		e\imy = frame(1, 24)
		e\deadImx = frame(4, 24)
		e\deadImy = e\imy
		e\width = 24
		e\height = 24
		
		e\hitBoxX = 6
		e\hitBoxY = 5
		e\hitBoxWidth = 12
		e\hitBoxHeight = 17
	End If
End Function

Function updateEnemy()
	For e.enemy = Each enemy
		If e\dead = 0 Then 
			For p.projectile = Each projectile
				If p\enemy = 0 And e\hitCount <= 0 Then 
					If collision(p\x, p\y, p\size, p\size, e\x+e\hitBoxX, e\y+e\hitBoxY, e\hitBoxWidth, e\hitBoxHeight) Then
						e\health = e\health - p\damage
						If e\health > 0 Then e\hitCount = 1
						p\destroy = 1
					End If
				End If
			Next
		End If
		
		If e\hitCount >= 1 Then
			e\hitCount = e\hitCount + 1
			If e\hitCount >= 4 Then e\hitCount = 0
		End If
		
		If e\health <= 0 Then 
			e\dead = 1
		End If
		
		If e\maxFireRate > 0 And e\attacking Then
			e\fireRate = e\fireRate + 1
			
			If e\fireRate >= e\maxFireRate Then
				e\shoot = 1
				e\fireRate = 0
			End If
		End If
		
		If e\typeOf = KNIGHT Then
			If e\shoot Then
				e\shootAngle = getAngleToPlayer(e\x+e\width/2, e\y+e\height/2)
				addProjectile(e\x+e\width/2-4, e\y+e\height/2-4, e\shootAngle, 5, 1, 50+frame(AngleToDirection(e\shootAngle, 1), 8), 76, 8, 1)
				e\shoot = 0
			End If
		End If
		
		If e\dead Then
			e\speed = 0
			e\maxFireRate = 0
			e\attacking = 0
			
			e\imx = e\deadImx
			e\imy = e\deadImy
		End If
		
		If e\destroy Then Delete e
	Next
End Function

Function drawEnemy()
	For e.enemy = Each enemy
		DrawImageRect(spritesheet, e\x, e\y, e\imx, e\imy, e\width, e\height)
		If e\hitCount > 0 Then 
			Color 255, 0, 0
			Rect e\x+e\hitBoxX, e\y+e\hitBoxY, e\hitBoxWidth, e\hitBoxHeight
		End If 
	Next
End Function 

Function update()
	updatePlayer()
	updateProjectile()
	updateEnemy()
End Function

Function draw()
	drawEnemy()
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
		
		If MouseHit(1) Then addEnemy(MouseX(), MouseY(), KNIGHT)
	Flip
Wend

FreeImage(spritesheet)