Dodawianie własnego telefonu do Android Studio, jeśli nie działa parowanie bezprzewodowe wbudowane w aplikację:
  1) Otworzyć losowy folder
  2) Shift + prawy przycisk myszy
  3) Otwórz tutaj okno programu PowerShell
  4) Wprowadź następujące komendy:
     -   cd C:\Users\Acer\AppData\Local\Android\Sdk\platform-tools
     -   ls
     -   ./adb pair (adres ip z portem, który wyświetla się w "sparuj urządzenie za pomocą kodu")

         --- potwierdzenie 6-cyfrowym kodem ---
         
         ![image](https://github.com/user-attachments/assets/330d2404-b915-4198-a360-c08e21f7fb27)

     -   ./adb connect (adres ip z portem urządzenia)
       
         ![image](https://github.com/user-attachments/assets/9ce9853c-a290-4624-b20b-b143d827e0c7)
