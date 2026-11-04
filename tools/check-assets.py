#!/usr/bin/env python3
import json, re, subprocess, struct
from pathlib import Path
root=Path(__file__).resolve().parents[1]
a=root/'src/main/resources/assets/betterthanherobrine'
sounds=json.loads((a/'sounds.json').read_text())
source=(root/'src/main/java/com/betterthanherobrine/events/Events.java').read_text()
used=set(re.findall(r'sound\(\d+,"([a-z_]+)"',source))
assert used<=sounds.keys(),used-sounds.keys()
for name,data in sounds.items():
 for variant in data['sounds']:
  namespace,path=variant['name'].split(':');assert namespace=='betterthanherobrine'
  ogg=a/'sounds'/f'{path}.ogg';assert ogg.is_file(),ogg
  info=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_entries','stream=codec_name,channels,sample_rate:format=duration','-of','json',str(ogg)]))
  assert info['streams'][0]['codec_name']=='vorbis' and info['streams'][0]['channels']==1
  assert .1<float(info['format']['duration'])<5
  subprocess.run(['ffmpeg','-nostdin','-v','error','-i',str(ogg),'-f','null','-'],check=True)
langs=[json.loads((a/'lang'/f'{lang}.json').read_text()) for lang in ['en_us','ru_ru']]
assert langs[0].keys()==langs[1].keys()
for lang in langs:
 for entry in sounds.values():assert entry['subtitle'] in lang
 for java in (root/'src').rglob('*.java'):
  for key in re.findall(r'"(bth\.[a-z.]+|key\.betterthanherobrine\.[a-z]+)"',java.read_text()):assert key in lang,(java,key)
for name in ['watcher','herobrine','doppelganger']:
 png=(a/f'textures/entity/{name}.png').read_bytes();assert png[:8]==b'\x89PNG\r\n\x1a\n';assert struct.unpack('>II',png[16:24])==(64,64)
mod=json.loads((root/'src/main/resources/fabric.mod.json').read_text());assert mod['id']=='betterthanherobrine' and mod['authors']==['zaan']
for kind,entries in mod['entrypoints'].items():
 for entry in entries:
  folder='client' if kind=='client' else 'main';assert (root/'src'/folder/'java'/Path(entry.replace('.','/')).with_suffix('.java')).is_file()
print(f'PASS: {len(sounds)} sound events, {sum(len(s["sounds"]) for s in sounds.values())} decoded mono Vorbis resources, 3 textures, matching EN/RU keys, Fabric entrypoint files.')
