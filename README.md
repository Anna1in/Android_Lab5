
# Лабораторна робота №5
# &nbsp;Виконала студентка 3 курсу, групи ІО-21, Любченко Анна.
&nbsp;&nbsp;&nbsp; На тему :" Дослідження роботи з вбудованими датчиками"
Мета роботи: ознайомитись з можливостями вбудованих датчиків мобільних пристроїв та дослідити способи їх використання для збору та обробки даних.
Робота була виконана на оцінку 20, за повними вимогами. (обрано 2 з запропонованих тем робіт: «будівельний рівень» з виведенням лінії горизонту та кутом нахилу; автоматичне регулювання яскравості та екрану в залежності від рівня освітлення) 

В даній роботі акцент саме на роботу з вбудованими датчиками, тому відповідно маємо тільки 1 файл інтерфейсу -activity_main.xml та 3 файли логіки програми - MainActivity.kt , ScreenControlSensorManager.kt, LevelSensorManager.kt.

З самого початку ми задаємо дозволи для роботи з датчиками, які запитуються або при запуску програми, якркз виклик спеціального вікна з налаштувань, або через AndroidManifest.xml.

Дозволи в AndroidManifest.xml:

            <uses-permission android:name="android.permission.WAKE_LOCK" />
            <uses-permission android:name="android.permission.WRITE_SETTINGS"
                tools:ignore="ProtectedPermissions" />

Перший дозволяє додатку використовувати WakeLock — це механізм, який дозволяє: вмикати або тримати екран увімкненим погасити екран без блокування системи. Друге дозволяє додатку змінювати системні налаштування — наприклад: яскравість екрана на глобальному рівні (для всього пристрою).

Для активації дозволів, програма перевіряє наявність доступів та запитує їх у самому пристрої:

![image](https://github.com/user-attachments/assets/b7215bbb-5424-4569-850e-f951b6cb72a8)

Релізовано у MainActivity.kt :

              if (!Settings.System.canWrite(this)) {
                  val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                  intent.data = Uri.parse("package:$packageName")
                  startActivity(intent)
              }

Опишемо конкретно методи для реалізації ідей.
## Будівельний рівень. 
Sensor.TYPE_ACCELEROMETER - Завдяки цій команді ми отримуємо дані від сенсору акеселерометра. 
У коді файлу відповідно є реєстрація сенсора :

              private val sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
              private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
              
              fun register() {
                  sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
              }
              
              fun unregister() {
                  sensorManager.unregisterListener(this)
              }

Та обробка даних, де відповідно співвідношенням осей Х та Y ми отримуємо лінію горизонту:

                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                            val x = it.values[0]
                            val y = it.values[1]
                
                            // Вираховуємо кут нахилу по осях
                            val angle = Math.toDegrees(atan2(x.toDouble(), y.toDouble())).toFloat()
                
                            // Виводимо кут у TextView
                            angleTextView.text = "Кут: %.2f°".format(angle)
                
                            // Повертаємо лінію горизонту (View) на цей кут
                            horizonLineView.rotation = -angle
                        }
                    }
                }

Потім всі результати відповідно виводяться до activity_main.xml.

## Яскравість та блокування екрану при наближенні 

Через Sensor.TYPE_LIGHT, Sensor.TYPE_PROXIMITY - отримуємо доступ до датчиків.
Для реалізації методу про зміну яскравості за рівнем освітлення.

            private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

У цій функції реалізовано реакцію на зміну освітленості, де задана відповідна реакція рівня яскравості на телефоні, при певному рівні освітлення.

            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                    val lux = event.values[0]
            
                    val layoutParams = activity.window.attributes
                    layoutParams.screenBrightness = when {
                        lux < 10 -> 0.1f
                        lux < 100 -> 0.4f
                        lux < 500 -> 0.7f
                        else -> 1.0f
                    }
                    activity.window.attributes = layoutParams
                }
            }

Для реалізації блокування екрану при наближенні:
Отримуємо доступ до датчиків - 

          private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
          private val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
          private var wakeLock: PowerManager.WakeLock? = null

Реакція на наближення, де задане вимкнення як реакція на наближення ближче, ніж 0.1f, та при віддаленні ми отримаємо згову ввімкнення.

          if (proximityValue == 0.0f) {
                            turnScreenOff()
                        } else {
                            turnScreenOn()
                        }
                  
          private fun turnScreenOff() {
              if (wakeLock?.isHeld == false) {
                  wakeLock?.acquire()
              }
          }
          
          private fun turnScreenOn() {
              if (wakeLock?.isHeld == true) {
                  wakeLock?.release()
              }
          }

Всі значення датчиків передавалось віртуальній машині через Extended Controls - Virtual Sensors.
Демонстрація: 

![image](https://github.com/user-attachments/assets/fa10351f-7012-48b7-984a-76555753234e)

![image](https://github.com/user-attachments/assets/2b3710dc-f260-4788-b2ba-6e1c7c24d7b8)

# Висновок: 

Програма виконує завдання відображення будівельного рівня, виведення кута нахилу, зміни яскравості в залежності від рівня освітлення та блокування екрану при наближенні об'єкта. Під час роботи, було отримано навички роботи з дозволами для отримання даних з потрібних датчиків. Було розроблено класи та функції для виконання заданого завдання та описано у звіті. Для виводу результатів було застосовано 1 інтерфейс, з віджетами TextView, View. Всі значення датчиків передавалось віртуальній машині через Extended Controls - Virtual Sensors.
# Контрольні питання:
&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;1. Наведіть приклади вбудованих датчиків та величини які з них можна зчитати.

 TYPE_LIGHT - Рівень освітлення в люксах (lx)

 TYPE_PROXIMITY - Відстань до об’єкта перед екраном (зазвичай в см)

 TYPE_ACCELEROMETER - Прискорення по осях X, Y, Z (м/с²)

TYPE_GYROSCOPE - Кутова швидкість обертання по осях X, Y, Z (рад/с)

TYPE_MAGNETIC_FIELD - Напруженість магнітного поля по осях X, Y, Z (мкТл)

TYPE_PRESSURE - Атмосферний тиск (гПа)

TYPE_AMBIENT_TEMPERATURE - Температура повітря (°C)

&nbsp;&nbsp;&nbsp;У віртуальній машині задавати значення сенсорів можливо в Extended Controls - Virtual Sensors.
![image](https://github.com/user-attachments/assets/7e43642b-74d7-4c88-81aa-6e87c235c3ff)
&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;2. Наведіть особливості роботи з вбудованими датчиками.

1. Обов'язково треба мати доступи, та перевряти доступ до них.

2. Для отримання даних треба під'єднатись до сенсора (sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI))

3. Регулювати частосу оновлення через константи - SENSOR_DELAY_NORMAL,SENSOR_DELAY_UI, SENSOR_DELAY_GAME, SENSOR_DELAY_FASTEST

4. Перед використанням перевіряти чи є сенсор.

5. Для меншого навантаження, реєструвати (onResume()) їх треба лише при необхідності та вчасно відписуватися (onPause()).
