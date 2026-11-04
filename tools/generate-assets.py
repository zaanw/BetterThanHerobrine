#!/usr/bin/env python3
"""Original, deterministic synthesis. Requires Python 3 and FFmpeg with libvorbis.
No recordings, voices, text, or third-party artwork are used.
"""
import json, math, random, struct, subprocess, tempfile, wave, zlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/betterthanherobrine'
RATE=22050
NAMES=['step','stone','knock','door','breath','whisper','rustle','drone']
def render(name,variant):
 rng=random.Random(719+NAMES.index(name)*100+variant)
 seconds={'step':.5,'stone':1.7,'knock':.6,'door':1.5,'breath':3.0,'whisper':3.0,'rustle':1.8,'drone':3.8}[name]
 data=[]; low=0
 for i in range(int(RATE*seconds)):
  t=i/RATE; noise=rng.uniform(-1,1);low=low*.94+noise*.06
  fade=min(1,t/.025)*min(1,(seconds-t)/.25)
  if name=='drone':v=(math.sin(t*2*math.pi*(43+variant*5))*.28+math.sin(t*2*math.pi*61)*.12)*math.sin(math.pi*t/seconds)**2
  elif name=='rustle':v=noise*.18*math.sin(math.pi*t/seconds)**2*(.5+.5*math.sin(t*23))
  elif name=='step':v=(low*3+math.sin(t*2*math.pi*(85+variant*7))*.2)*math.exp(-t*15)
  elif name=='knock':v=(math.sin(2*math.pi*t*(180+variant*19))*.6+noise*.25)*math.exp(-t*20)
  elif name=='stone':v=(noise*.2+math.sin(t*2*math.pi*(120+t*31))*.1)*math.exp(-t*4)*(1+math.sin(t*39))
  elif name=='door':v=(low*1.2+math.sin(t*2*math.pi*(210-t*70))*.18)*(math.sin(math.pi*t/seconds)**2)
  elif name=='breath':v=low*2.5*(math.sin(math.pi*t/seconds)**2)*(1+math.sin(t*5))*.45
  else:v=(noise-low)*.16*(math.sin(math.pi*t/seconds)**2)*(1+math.sin(t*13))*.5
  data.append(struct.pack('<h',int(max(-.75,min(.75,v*fade*.55))*32767)))
 return b''.join(data)
(ROOT/'sounds').mkdir(parents=True,exist_ok=True)
for name in NAMES:
 for variant in range(2):
  with tempfile.TemporaryDirectory() as temp:
   wav=Path(temp)/'sound.wav'
   with wave.open(str(wav),'wb') as f:f.setnchannels(1);f.setsampwidth(2);f.setframerate(RATE);f.writeframes(render(name,variant))
   subprocess.run(['ffmpeg','-nostdin','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4','-fflags','+bitexact','-flags:a','+bitexact',str(ROOT/'sounds'/f'{name}_{variant}.ogg')],check=True)
(ROOT/'sounds.json').write_text(json.dumps({name:{'subtitle':f'bth.subtitle.{name}','sounds':[{'name':f'betterthanherobrine:{name}_{i}','weight':1,'attenuation_distance':48} for i in range(2)]} for name in NAMES},indent=2)+'\n')
# A 64x64 vanilla humanoid UV atlas. The texture is entirely original pixel data.
pixels=[];rng=random.Random(346)
for y in range(64):
 row=bytearray()
 for x in range(64):
  n=rng.randrange(9);c=(18+n,20+n,25+n,255)
  if 8<=x<24 and 8<=y<16:c=(39+n,38+n,42+n,255)
  if y in (11,12) and x in (9,10,13,14):c=(225,231,236,255)
  row.extend(c)
 pixels.append(b'\x00'+row)
def chunk(name,data):return struct.pack('>I',len(data))+name+data+struct.pack('>I',zlib.crc32(name+data)&0xffffffff)
texture=ROOT/'textures/entity/watcher.png';texture.parent.mkdir(parents=True,exist_ok=True)
texture.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',64,64,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(b''.join(pixels)))+chunk(b'IEND',b''))
langs={
 'en_us':{'bth.help':'/bth help | status | debug | summon | event <id> | intensity <0..2> | reset','bth.busy':'Event unavailable: disabled, unknown ID, or active scene limit.','bth.enable':'Enable Horror','bth.sounds':'Atmospheric Sounds','bth.volume':'Horror Volume','bth.visuals':'Visual Effects','bth.fog':'Atmospheric Veil','bth.screen':'Screen Effects','bth.performance':'Performance Mode','bth.anomalies':'World Anomalies','bth.intensity':'Horror Intensity','bth.frequency':'Entity Frequency','key.betterthanherobrine.settings':'BetterThanHerobrine settings','key.category.betterthanherobrine.settings':'BetterThanHerobrine','entity.betterthanherobrine.watcher':'Watcher',**{f'bth.subtitle.{k}':v for k,v in zip(NAMES,['Unfamiliar footsteps','Stone shifts','A quiet knock','A distant door creaks','Uneven breathing','An indistinct whisper','Something rustles','A low resonance'])}},
 'ru_ru':{'bth.help':'/bth help | status | debug | summon | event <id> | intensity <0..2> | reset','bth.busy':'Событие недоступно: отключено, неизвестно или достигнут лимит сцен.','bth.enable':'Включить хоррор','bth.sounds':'Атмосферные звуки','bth.volume':'Громкость хоррора','bth.visuals':'Визуальные эффекты','bth.fog':'Атмосферная дымка','bth.screen':'Экранные эффекты','bth.performance':'Режим производительности','bth.anomalies':'Аномалии мира','bth.intensity':'Интенсивность хоррора','bth.frequency':'Частота наблюдателя','key.betterthanherobrine.settings':'Настройки BetterThanHerobrine','key.category.betterthanherobrine.settings':'BetterThanHerobrine','entity.betterthanherobrine.watcher':'Наблюдатель',**{f'bth.subtitle.{k}':v for k,v in zip(NAMES,['Незнакомые шаги','Шорох камня','Тихий стук','Вдалеке скрипит дверь','Неровное дыхание','Неразборчивый шёпот','Что-то шуршит','Низкий гул'])}}
}
extra={
 'en_us':{'bth.herobrine_joined':'Herobrine joined the game.','bth.denied':'Access denied: zaanw and server operator permission are required.','bth.reduced':'Reduce Visual Intensity','bth.admin.spawn':'Spawn Herobrine','bth.admin.remove':'Remove Herobrine','bth.admin.random':'Trigger Random Event','bth.admin.nightmare':'Start Nightmare Sequence','bth.admin.stop':'Stop All Events','bth.admin.debug':'Toggle Debug Overlay','bth.admin.entities':'Show Active Entities','bth.admin.timers':'Show Event Timers','bth.admin.reset':'Reset Horror Director','bth.admin.run':'Run','key.betterthanherobrine.admin':'Horror admin panel (server authorization)','entity.betterthanherobrine.herobrine':'Herobrine'},
 'ru_ru':{'bth.herobrine_joined':'Херобрин присоединился к игре.','bth.denied':'Нет доступа: требуются ник zaanw и права оператора сервера.','bth.reduced':'Уменьшить визуальные эффекты','bth.admin.spawn':'Вызвать Херобрина','bth.admin.remove':'Убрать Херобрина','bth.admin.random':'Случайное событие','bth.admin.nightmare':'Начать кошмар','bth.admin.stop':'Остановить все события','bth.admin.debug':'Переключить отладочный оверлей','bth.admin.entities':'Показать активные сущности','bth.admin.timers':'Показать таймеры событий','bth.admin.reset':'Сбросить Horror Director','bth.admin.run':'Запуск','key.betterthanherobrine.admin':'Панель администратора (доступ сервера)','entity.betterthanherobrine.herobrine':'Херобрин'}
}
for lang,values in langs.items():values.update(extra[lang])
for lang,values in langs.items():(ROOT/'lang'/f'{lang}.json').write_text(json.dumps(values,ensure_ascii=False,indent=2)+'\n')
print('Generated 16 original OGG files, dark humanoid texture, sounds.json and EN/RU translations.')

# Original Steve-like palette painted into the humanoid UV layout, not copied from Mojang assets.
for variant in ['herobrine','doppelganger']:
 rows=[];rng=random.Random(94)
 for y in range(64):
  row=bytearray()
  for x in range(64):
   n=rng.randrange(-4,5);c=(28,23,21,255)
   if y<16 and x<32:
    c=(50+n,36+n,27+n,255)
    if 8<=x<16 and 9<=y<16:c=(165+n,119+n,86+n,255)
    if y==11 and x in [9,10,13,14]:c=(240,242,244,255)
    if y==14 and 10<=x<14:c=(76,47,35,255)
   elif 16<=y<32:
    c=(28+n,131+n,140+n,255) if x>=16 else (44+n,46+n,105+n,255)
    if x>=40 and y>=23:c=(165+n,119+n,86+n,255)
   elif 48<=y<64:
    c=(44+n,46+n,105+n,255) if x<16 else (165+n,119+n,86+n,255)
   else:c=(0,0,0,0)
   if x>=32 and y<16:c=(0,0,0,0)
   if variant=='doppelganger':c=tuple(int(v*.55) for v in c[:3])+(c[3],)
   row.extend(c)
  rows.append(b'\x00'+row)
 (ROOT/'textures/entity'/f'{variant}.png').write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',64,64,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(b''.join(rows)))+chunk(b'IEND',b''))
