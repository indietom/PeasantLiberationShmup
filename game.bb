Const WIN_W=32*15, WIN_H=32*14

Global frameRate = 60

Global fullScreen = 2

Graphics WIN_W, WIN_H, 8, fullScreen

SeedRnd MilliSecs()

Global frametimer=CreateTimer(frameRate)
Global starttime=MilliSecs(),elapsedtime,fpscounter,curfps

Function setFrameRateCap(cap)
	frametimer=CreateTimer(cap)
	starttime=MilliSecs()
End Function

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
	
	Field movingToX
	Field movingToY
	
	Field dead
	
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
		For pr.projectile = Each projectile
			If pr\enemy Then
				If collision(pr\x, pr\y, pr\size, pr\size, p\x, p\y, 24, 24) Then
					p\dead = 1
					pr\destroy = 1
				End If
			End If
		Next
		
		p\movingToX = 0
		p\movingToY = 0
		
		If KeyDown(p\leftKey) And p\x >= p\speed Then
			p\x = p\x - p\speed
			p\movingToX = p\movingToX - p\speed
		End If
		
		If KeyDown(p\rightKey) And p\x <= WIN_W - 24 - p\speed Then
			p\x = p\x + p\speed
			p\movingToX = p\movingToX + p\speed
		End If
		
		If KeyDown(p\upKey) Then
			p\y = p\y - p\speed
			p\movingToY = p\movingToY - p\speed
		End If
		
		If KeyDown(p\downKey) Then
			p\y = p\y + p\speed
			p\movingToY = p\movingToY + p\speed
		End If
		
		If KeyHit(p\shootKey) Then
			If p\gunType = 0 Then
				addProjectile(p\x+12-4, p\y+12-4, -90, p\speed+4, 1, frame(2, 24), 51, 8, 0)
			End If
		End If
		
		offsetX = lerp(offsetX, (Float(p\x - win_w/2)) * parallelProcent, 0.1)
		
		If p\destroy Then Delete p
	Next
End Function

Function drawPlayer()
	For p.player = Each player
		DrawImageRect(spritesheet, p\x+p\shadowOffsetX-offsetX, p\y+p\shadowOffsetY, frame(1, 24), p\imy, 24, 24)
		DrawImageRect(spritesheet, p\x-offsetX, p\y, p\imx, p\imy, 24, 24)
		Color 255, 255, 255
	Next
End Function

Function getDistanceToPlayer#(x2, y2)
	For p.player = Each player
		Return distanceTo(p\x, p\y, x2, y2)
	Next
End Function

Function getAngleToPlayer#(x2#, y2#, time)
	Local px# 
	Local py# 
	
	For p.player = Each player
		px# = p\movingToX*time+p\x + 16
		py# = p\movingToY*time+p\y + 16
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
		
		If p\x - offsetX < -p\size Or p\x - offsetX >= WIN_W Or p\y < -p\size Or p\y > WIN_H Then p\destroy = 1 
		
		If p\destroy Then Delete p
	Next
End Function

Function drawProjectile()
	For p.projectile = Each projectile
		DrawImageRect(spritesheet, p\x-offsetX, p\y, p\imx, p\imy, p\size, p\size)
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
	
	Field currentFrame
	Field animationCount
	Field maxAnimationCount
	Field maxFrame
	
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
		
		e\maxAnimationCount = 4
		e\maxFrame = 4
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
				e\shootAngle = getAngleToPlayer(e\x+e\width/2, e\y+e\height/2, getDistanceToPlayer(e\x, e\y)/8)
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
		Else
			If e\maxFrame > 0 Then
				e\animationCount = e\animationCount + 1
				If e\animationCount >= e\maxAnimationCount Then
					e\currentFrame = e\currentFrame + 1
					If e\currentFrame >= e\maxFrame Then e\currentFrame = 0
					e\animationCount = 0
				End If
				e\imx = frame(e\currentFrame, e\width)
			End If
		End If
		
		If e\destroy Then Delete e
	Next
End Function

Function drawEnemy()
	For e.enemy = Each enemy
		DrawImageRect(spritesheet, e\x-offsetX, e\y, e\imx, e\imy, e\width, e\height)
		If e\hitCount > 0 Then 
			Color 255, 0, 0
			Rect e\x+e\hitBoxX-offsetX, e\y+e\hitBoxY, e\hitBoxWidth, e\hitBoxHeight
		End If 
	Next
End Function 

Function enemiesAlive()
	Local count
	
	For e.enemy = Each enemy 
		If e\dead = 0 Then count = count + 1
	Next
	
	Return count
End Function

Function aimAtNearestEnemy#(x2#, y2#)
	Local ex#
	Local ey#
	
	If enemiesAlive() > 1 Then 
		For e.enemy = Each enemy
			For e2.enemy = Each enemy
				If e <> e2 Then
					If e\dead = 0 And e2\dead = 0 Then 
						If distanceTo(e\x, e\y, x2, y2) < distanceTo(e2\x, e2\y, x2, y2) Then
							ex = e\x
							ey = e\y
						End If
					End If
				End If	
			Next
		Next
	End If
	
	If enemiesAlive() = 1 Then
		For e.enemy = Each enemy
			If e\dead = 0 Then
				ex = e\x
				ey = e\y
			End If 
		Next
	End If
	
	Return ATan2(ey - y2, ex - x2)
End Function 

Type helper
	Field x#
	Field y#
	
	Field liberated
	
	Field lifeTime
	Field maxLifeTime
	
	Field health
	Field dead
	
	Field fireRate
	Field maxFireRate
	
	Field shootAngel#
	
	Field targetPositionX#
	Field targetPositionY#
	
	Field hitBoxX
	Field hitBoxY
	Field hitBoxWidth
	Field hitBoxHeight
	
	Field hitCount
	
	Field imx
	Field imy
	
	Field currentFrame
	Field animationCount
	Field maxAnimationCount
	Field maxFrame
	
	Field destroy
End Type

Function addHelper(x2#, y2#)
	h.helper = New helper
	h\x = x2
	h\y = y2
	
	h\health = 2
	
	h\maxFireRate = 16*4
	
	Local targetDistance# = Rnd(16, 64)
	Local targetAngle# = -45 * Rand(8)
	
	h\targetPositionX = Cos(targetAngle) * targetDistance
	h\targetPositionY = Sin(targetAngle) * targetDistance
	
	h\imx = frame(0, 24)
	h\imy = frame(0, 24)
	
	h\maxFrame = 4
	
	h\hitBoxX = 6
	h\hitBoxY = 5
	h\hitBoxWidth = 12
	h\hitBoxHeight = 17
End Function

Function updateHelper()
	For h.helper = Each helper
		If h\dead = 0 Then
			h\imx = frame(h\currentFrame, 24)
		Else
			h\imx = frame(4, 24)
		End If
		
		If h\fireRate >= h\maxFireRate And h\dead = 0 Then
			If enemiesAlive() > 0 Then addProjectile(h\x+12-2, h\y+12-2, aimAtNearestEnemy(h\x, h\y)+Rnd(-8, 8), 5, 1, frame(3, 24), frame(2, 24), 4, 0)
			h\fireRate = 0 
		End If 
		
		If h\liberated Then
			h\fireRate = h\fireRate + 1
		
			If h\dead = 0 Then 
				For p.player = Each player
					h\x = lerp(h\x, p\x+h\targetPositionX, 0.1)
					h\y = lerp(h\y, p\y+h\targetPositionY, 0.1)
				Next
			End If
			
			For e.enemy = Each enemy 				
				If e\dead = 0 Then
					If collision(e\x+e\hitBoxX, e\y+e\hitBoxY, e\hitBoxWidth, e\hitBoxHeight, h\x+h\hitboxX, h\y+h\hitBoxY, h\hitBoxWidth, h\hitboxHeight) Then 
						h\health = 0
					End If
				End If
			Next
				
			h\animationCount = h\animationCount + 1
		Else
			For p.player = Each player
				If collision(p\x, p\y, 24, 24, h\x, h\y, 24, 24) Then
					h\liberated = 1
				End If
			Next
		End If
		
		If h\dead = 0 And h\hitCount <= 0 Then 
			For pr.projectile = Each projectile 
				If collision(pr\x, pr\y, pr\size, pr\size, h\x+h\hitboxX, h\y+h\hitBoxY, h\hitBoxWidth, h\hitboxHeight) Then
					If h\liberated And pr\enemy = 1 Or h\liberated = 0 And pr\enemy = 0 Then 
						h\health = h\health - pr\damage
						h\hitCount = 1
						pr\destroy = 1
					End If
				End If
			Next
		End If
		
		If h\health <= 0 Then h\dead = 1
		
		If h\hitCount >= 1 Then 
			h\hitCount = h\hitCount + 1
			If h\hitCount >= 4 Then h\hitCount = 0
		End If
		
		If h\animationCount >= 4 And h\dead = 0 Then
			h\currentFrame = h\currentFrame + 1
			If h\currentFrame >= h\maxFrame Then h\currentFrame = 0
			h\animationCount = 0
		End If
	Next
End Function

Function drawHelper()
	For h.helper = Each helper
		DrawImageRect(spritesheet, h\x-offsetX, h\y, h\imx, h\imy, 24, 24)	
		If h\hitCount > 0 Then 
			Color 255, 0, 0
			Rect h\x+h\hitboxX-offsetX, h\y+h\hitBoxY, h\hitBoxWidth, h\hitboxHeight
		End If
	Next
End Function

Const TREE = 0
Const TREE_WITH_APPLE = 1

Type backgroundObject
	Field x#
	Field y#
	
	Field imx
	Field imy
	Field width
	Field height
	
	Field typeOf
	
	Field destroy 
End Type

Function addBackgroundObject(x2#, y2#, typeOf2)
	b.backgroundObject = New backgroundObject
	b\x = x2
	b\y = y2
	
	b\typeOf = typeOf2
	
	If b\typeOf = TREE Then
		b\imx = 251
		b\imy = 1
		b\width = 24
		b\height = 48
	End If
	
	If b\typeOf = TREE_WITH_APPLE Then
		b\imx = 251
		b\imy = 50
		b\width = 24
		b\height = 48
	End If
End Function

Function updateBackgroundObject()
	For b.backgroundObject = Each backgroundObject
		
		If b\destroy Then Delete b
	Next
End Function

Function drawBackgroundObject()
	For b.backgroundObject = Each backgroundObject
		DrawImageRect(spritesheet, b\x-offsetX, b\y, b\imx, b\imy, b\width, b\height)
	Next
End Function 

Const HOUSE = 0

Type building
	Field x#
	Field y#
	
	Field imx
	Field imy
	Field width
	Field height
	
	Field deadImx
	Field deadImy
	
	Field dead
	Field health
	
	Field destroy
End Type

Function addBuilding()

End Function

Function updateBuilding()

End Function

Function drawBuilding()
	For b.building = Each building
		DrawImageRect(spritesheet, b\x-offsetX, b\y, b\imx, b\imy, b\width, b\height)
	Next
End Function

Global offsetX#
Global parallelProcent# = 0.5

Function update()
	updatePlayer()
	updateProjectile()
	updateEnemy()
	updateHelper()
	updateBackgroundObject()
End Function

Function draw()
	drawBackgroundObject()
	drawEnemy()
	drawHelper()
	drawProjectile()
	drawPlayer()
End Function 

addPlayer(WIN_W/2, WIN_H/2)

For i = 0 To 50 
	addBackgroundObject(Rnd(-300, WIN_W+300), Rnd(WIN_H), Rand(0, 1))
Next

While Not KeyHit(1)
	Cls 
		WaitTimer(frametimer)
		Color 102, 255, 119
		Rect -offsetX - (Float(WIN_W) * parallelProcent), 0, WIN_W+(Float(WIN_W) * parallelProcent)*2, WIN_H
		;Rect -offsetX, 0, WIN_W, WIN_H
		draw()
		update()
		
		If MouseHit(1) Then addEnemy(MouseX(), MouseY(), KNIGHT)
		If MouseHit(2) Then addHelper(MouseX(), MouseY())
	Flip
Wend

FreeImage(spritesheet)