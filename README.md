# Team Task Manager - Nowoczesna Aplikacja do Zarządzania Zadaniami

![Team Task Manager](https://img.shields.io/badge/Status-Gotowa%20do%20Produkcji-green)
![Frontend](https://img.shields.io/badge/Frontend-React%20TypeScript-blue)
![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-brightgreen)
![Database](https://img.shields.io/badge/Database-H2-orange)
![Architecture](https://img.shields.io/badge/Architecture-Service%20Layer-purple)

## 🚀 Przegląd

Team Task Manager to nowoczesna, animowana aplikacja internetowa do zarządzania zadaniami zespołowymi zbudowana z wykorzystaniem najnowszych technologii. Aplikacja charakteryzuje się pięknym designem glassmorphism, bogatymi animacjami, wszechstronnymi możliwościami zarządzania projektami oraz solidną architekturą warstwy usług.

## ✨ Kluczowe Funkcje

### 🎨 Nowoczesny Design
- **Interfejs Glassmorphism** z efektami backdrop-filter
- **Animowane tło** z unoszącymi się cząsteczkami
- **Responsywny design** działający na wszystkich urządzeniach
- **Bogate animacje** wykorzystujące naszą sprawdzoną strategię dwóch bibliotek ([zobacz ANIMATION_STRATEGY.md](ANIMATION_STRATEGY.md))
  - **Framer Motion** dla wszystkich animacji UI i interakcji
  - **Lottie React** dla złożonych animacji wektorowych i ilustracji
- **Piękne gradienty** i niestandardowe właściwości CSS

### 📊 Zarządzanie Zadaniami
- **Tablica Kanban** z funkcją przeciągnij i upuść
- **Operacje CRUD zadań** (Tworzenie, Odczyt, Aktualizacja, Usuwanie)
- **System priorytetów** (Wysoki, Średni, Niski) z wizualnymi wskaźnikami
- **Śledzenie statusu** (Do Zrobienia, W Trakcie, Ukończone)
- **Zarządzanie terminami** z wykrywaniem przeterminowanych
- **Przypisywanie użytkowników** z profilami członków zespołu
- **Automatyczne logowanie zmian** dla wszystkich operacji na zadaniach
- **Zaawansowane filtrowanie zadań** według użytkownika, statusu i priorytetu

### 📈 Zaawansowana Analityka
- **Interaktywny Dashboard** z statystykami w czasie rzeczywistym
- **Okrągłe wykresy postępu** dla wskaźników ukończenia
- **Rozkład priorytetów i statusów** z animowanymi paskami postępu
- **Śledzenie wydajności zespołu** z indywidualnymi metrykami
- **Analityka ukończenia projektów**
- **Statystyki użytkowników** ze współczynnikami ukończenia i podziałem zadań

### 🔔 Inteligentny System Powiadomień
- **Centrum powiadomień w czasie rzeczywistym** z animowaną ikoną dzwonka
- **Alerty przeterminowanych zadań**
- **Przypomnienia o terminach** (dzisiaj i jutro)
- **Powiadomienia o osiągnięciach** dla kamieni milowych postępu
- **Animowane znaczki powiadomień**

### 📄 Eksport Danych
- **Eksport CSV** dla zadań, raportów zespołowych i podsumowań projektów
- **Kompleksowe raportowanie** ze szczegółowymi statystykami
- **Wizualizacja danych** dla lepszego wglądu

### 🏗️ Architektura Warstwy Usług
- **Czyste rozdzielenie** obowiązków między kontrolerami, usługami i repozytoriami
- **Enkapsulacja logiki biznesowej** w dedykowanych klasach usług
- **Kompleksowa walidacja** i obsługa błędów
- **Automatyczne śledzenie zmian** dla ścieżek audytu
- **Obszerne testy jednostkowe** z wysokim pokryciem

## 🛠 Stos Technologiczny

### Backend
- **Java 17** ze Spring Boot 3.x
- **Architektura Warstwy Usług** z czystym rozdzieleniem obowiązków
- **Spring Data JPA** dla operacji bazodanowych
- **Jakarta Validation** dla walidacji danych
- **Type-safe enums** dla statusów i priorytetów
- **Baza danych H2** dla rozwoju i testowania
- **REST API** z serializacją JSON
- **Gradle** do zarządzania buildem
- **JUnit 5 & Mockito** dla kompleksowych testów

### Frontend
- **React 18** z TypeScript
- **Framer Motion** dla zaawansowanych animacji
- **React Beautiful DnD** dla przeciągnij i upuść
- **Lottie React** dla animowanych ikon
- **Nowoczesny CSS** z niestandardowymi właściwościami i glassmorphism

## 🏗 Architektura

### Architektura Warstwy Usług
```
Warstwa Kontrolerów (HTTP/REST)
    ↓
Warstwa Usług (Logika Biznesowa)
    ↓
Warstwa Repozytoriów (Dostęp do Danych)
    ↓
Warstwa Bazy Danych (H2)
```

### Struktura Backend
```
src/main/java/org/example/
├── entities/
│   ├── Task.java              # Encja zadania z walidacją
│   ├── User.java              # Encja użytkownika z relacjami
│   ├── TaskStatus.java        # Type-safe enum statusu
│   ├── TaskPriority.java      # Type-safe enum priorytetu
│   └── UserStats.java         # Model statystyk użytkownika
├── services/
│   ├── TaskService.java       # Interfejs logiki biznesowej zadań
│   ├── TaskServiceImpl.java   # Implementacja serwisu zadań
│   ├── UserService.java       # Interfejs logiki biznesowej użytkowników
│   └── UserServiceImpl.java   # Implementacja serwisu użytkowników
├── controllers/
│   ├── TaskController.java    # Endpointy REST zadań
│   ├── UserController.java    # Endpointy REST użytkowników
│   └── EnumController.java    # API wartości enum
├── repositories/
│   ├── TaskRepository.java    # Dostęp do danych zadań
│   └── UserRepository.java    # Dostęp do danych użytkowników
├── validation/
│   └── GlobalExceptionHandler.java # Globalna obsługa błędów
└── Main.java                  # Punkt wejścia aplikacji Spring Boot
```

### Struktura Frontend
```
frontend/src/
├── components/
│   ├── Dashboard.tsx        # Dashboard analityczny z wykresami
│   ├── StatsCard.tsx       # Animowane karty statystyk
│   ├── TaskCard.tsx        # Komponenty pojedynczych zadań
│   └── NotificationCenter.tsx # Inteligentny system powiadomień
├── utils/
│   └── csvExport.ts        # Narzędzia eksportu danych
├── lottie/
│   └── avatar.json         # Dane animowanego awatara
└── App.tsx                 # Główny komponent aplikacji
```

## 🚀 Rozpoczęcie Pracy

### Wymagania Wstępne
- Java 17 lub wyższa
- Node.js 16+ i npm
- Git

### Instalacja

1. **Sklonuj repozytorium**
```bash
git clone <repository-url>
cd Team-Task-Manager
```

2. **Uruchom backend**
```bash
./gradlew bootRun
```
Backend zostanie uruchomiony na `http://localhost:8080`

3. **Uruchom frontend** (w nowym terminalu)
```bash
cd frontend
npm install
npm start
```
Frontend zostanie uruchomiony na `http://localhost:3000`

### Przykładowe Dane
Aplikacja zawiera przykładowych użytkowników i zadania:
- **Użytkownicy**: Jan Kowalski, Anna Nowak, Piotr Wiśniewski
- **Zadania**: Różne poziomy priorytetów i statusów
- **Dane demonstracyjne** do testowania wszystkich funkcji

## 🎯 Główne Funkcjonalności

### 1. Zarządzanie Zadaniami
- Tworzenie zadań z tytułem, opisem, priorytetem, terminem
- Przypisywanie zadań członkom zespołu
- Przeciąganie i upuszczanie między kolumnami statusów
- Aktualizacje statusu w czasie rzeczywistym
- **Automatyczne logowanie zmian** z znacznikami czasu i opisami
- **Zaawansowane filtrowanie** według użytkownika, statusu, priorytetu
- **Przypisywanie/odpisywanie zadań** ze ścieżką audytu

### 2. Zarządzanie Użytkownikami
- **Operacje CRUD użytkowników** z walidacją
- **Walidacja unikalności** email i nazwy użytkownika
- **Obliczanie statystyk użytkowników** (współczynniki ukończenia, podział zadań)
- **Automatyczne odpisywanie zadań** przy usuwaniu użytkowników
- **Wyszukiwanie użytkowników** według email i nazwy użytkownika

### 3. Analityka Dashboard
- Wskaźnik ukończenia projektu z okrągłymi wykresami
- Wizualizacja rozkładu priorytetów
- Metryki wydajności zespołu
- Aktualizacje statystyk w czasie rzeczywistym
- **Indywidualne statystyki użytkowników** ze szczegółowymi podziałami

### 4. System Powiadomień
- Animowany dzwonek powiadomień z licznikiem znaczków
- Inteligentne alerty dla przeterminowanych zadań
- Przypomnienia o terminach
- Powiadomienia o osiągnięciach postępu
- Rozszerzalne centrum powiadomień

### 5. Eksport Danych
- Eksport zadań do formatu CSV
- Generowanie raportów wydajności zespołu
- Tworzenie dokumentów podsumowania projektu
- Pobieranie z nazwami plików z znacznikami czasu

## 🎨 Funkcje Designu

### Animacje
- **Przejścia stron** z animacjami schodkowymi
- **Efekty hover** na elementach interaktywnych
- **Stany ładowania** z płynnymi przejściami
- **Feedback przeciągnij i upuść** z efektami rotacji
- **Animacje powiadomień** z fizyką sprężyn

### Design Wizualny
- **Karty glassmorphism** z rozmyciem tła
- **Gradienty tła** i obramowania
- **Animowane unoszące się cząsteczki**
- **Responsywne układy siatki**
- **Nowoczesna typografia** z czcionką Inter

## 🔧 Endpointy API

### Zarządzanie Zadaniami
- `GET /api/tasks` - Pobierz wszystkie zadania
- `POST /api/tasks` - Utwórz nowe zadanie (z walidacją)
- `PUT /api/tasks/{id}` - Aktualizuj zadanie (z logowaniem zmian)
- `DELETE /api/tasks/{id}` - Usuń zadanie
- `GET /api/tasks/{id}` - Pobierz szczegóły zadania
- `GET /api/tasks/user/{userId}` - Pobierz zadania według użytkownika
- `GET /api/tasks/status/{status}` - Pobierz zadania według statusu
- `GET /api/tasks/priority/{priority}` - Pobierz zadania według priorytetu
- `PUT /api/tasks/{taskId}/assign/{userId}` - Przypisz zadanie użytkownikowi
- `PUT /api/tasks/{taskId}/unassign` - Odpisz zadanie
- `PUT /api/tasks/{taskId}/status?status={status}` - Zmień status zadania
- `PUT /api/tasks/{taskId}/priority?priority={priority}` - Zmień priorytet zadania

### Zarządzanie Użytkownikami
- `GET /api/users` - Pobierz wszystkich użytkowników
- `POST /api/users` - Utwórz nowego użytkownika (z walidacją)
- `PUT /api/users/{id}` - Aktualizuj użytkownika (z walidacją unikalności)
- `DELETE /api/users/{id}` - Usuń użytkownika (z czyszczeniem zadań)
- `GET /api/users/{id}` - Pobierz szczegóły użytkownika
- `GET /api/users/{userId}/stats` - Pobierz statystyki użytkownika
- `GET /api/users/email/{email}` - Znajdź użytkownika według email
- `GET /api/users/username/{username}` - Znajdź użytkownika według nazwy
- `GET /api/users/check-email/{email}` - Sprawdź istnienie email
- `GET /api/users/check-username/{username}` - Sprawdź istnienie nazwy użytkownika
- `GET /api/users/with-tasks` - Pobierz użytkowników z przypisanymi zadaniami
- `GET /api/users/without-tasks` - Pobierz użytkowników bez zadań
- `POST /api/users/seed` - Zainicjuj przykładowe dane

### Wartości Enum
- `GET /api/enums/all` - Pobierz wszystkie wartości enum z metadanymi
- `GET /api/enums/task-statuses` - Pobierz wartości statusów zadań
- `GET /api/enums/task-priorities` - Pobierz wartości priorytetów zadań

## 🧪 Testowanie

### Testy Jednostkowe
- **TaskServiceTest** - Kompleksowe testowanie warstwy usług
- **UserServiceTest** - Testowanie logiki biznesowej użytkowników
- **ValidationTest** - Testowanie walidacji danych
- **EnumTest** - Testowanie type-safe enum

### Pokrycie Testów
- Logika biznesowa warstwy usług
- Scenariusze walidacji
- Obsługa błędów
- Funkcjonalność logowania zmian
- Obliczanie statystyk

### Uruchamianie Testów
```bash
./gradlew test
```

## 📱 **Responsywny Design**

Aplikacja jest w pełni responsywna z:
- **Podejście mobile-first**
- **Elastyczne układy siatki**
- **Interakcje przyjazne dla dotyku**
- **Animacje zoptymalizowane dla GPU** na urządzeniach mobilnych ([zobacz Przewodnik Wydajności](ANIMATION_PERFORMANCE_GUIDE.md))
- **Adaptacyjne rozmiary komponentów**

## 📚 **Dokumentacja**

- **[Strategia Animacji](ANIMATION_STRATEGY.md)** - Kompleksowa architektura animacji i podejście dwóch bibliotek
- **[Przewodnik Wydajności](ANIMATION_PERFORMANCE_GUIDE.md)** - Techniki optymalizacji 60fps i akceleracja GPU
- **[Przewodnik Warstwy Usług](SERVICE_LAYER_GUIDE.md)** - Szczegółowa dokumentacja architektury
- **[Przewodnik Walidacji](VALIDATION_GUIDE.md)** - Walidacja danych i implementacja enum
- **Dokumentacja API** - Dostępna poprzez eksplorację endpointów

## 🎭 **Pokaz Animacji**

### Wydajność Zoptymalizowana pod GPU
- **Animacje 60fps** używające tylko `transform` i `opacity`
- **Zero layout thrashing** z akceleracją sprzętową
- **Wydajność adaptacyjna dla urządzenia** na podstawie możliwości
- **Narzędzia monitorowania w czasie rzeczywistym** dla rozwoju

### Mikro-interakcje
- Efekty hover przycisków ze skalowaniem i przejściami kolorów
- Animacje kart z efektami podnoszenia i cienia
- Spinnery ładowania z animacjami CSS
- Walidacja formularzy z efektami potrząsania

## 📊 Funkcje Wydajności

- **Lazy loading** dla komponentów
- **Zoptymalizowane animacje** z transform i opacity
- **Efektywne re-rendery** z React.memo i useMemo
- **Płynne animacje 60fps** z akceleracją GPU

## 🛡 Funkcje Gotowe do Produkcji

- **Obsługa błędów** z graceful fallbacks
- **Stany ładowania** dla lepszego UX
- **Bezpieczeństwo typów** z TypeScript
- **Kompatybilność międzyprzeglądarkowa**
- **Uwagi na dostępność**

## 🎯 Osiągnięte Cele Projektu

✅ **Nowoczesny Interfejs** - Design glassmorphism z bogatymi animacjami  
✅ **Zarządzanie Zadaniami** - Kompletne operacje CRUD z przeciągnij i upuść  
✅ **Współpraca Zespołowa** - Przypisywanie użytkowników i śledzenie wydajności zespołu  
✅ **Wizualizacja Danych** - Interaktywne wykresy i statystyki w czasie rzeczywistym  
✅ **Inteligentne Powiadomienia** - Automatyczne alerty i śledzenie postępu  
✅ **Możliwości Eksportu** - Eksporty CSV do analizy danych  
✅ **Responsywny Design** - Działa perfekcyjnie na wszystkich urządzeniach  
✅ **Jakość Produkcyjna** - Obsługa błędów, stany ładowania, bezpieczeństwo typów  

## 🔮 Przyszłe Ulepszenia

- Współpraca w czasie rzeczywistym z WebSockets
- Zaawansowane możliwości filtrowania i wyszukiwania
- Załączniki plików do zadań
- Funkcjonalność śledzenia czasu
- Integracja z zewnętrznymi systemami kalendarzy
- Zaawansowane raportowanie z wykresami
- Wsparcie dla wielu projektów
- Przełącznik motywu ciemny/jasny

## 👥 Zespół i Wkłady

Ten projekt demonstruje nowoczesne praktyki full-stack development z:
- Czystą architekturą i rozdzieleniem obowiązków
- Kompleksową obsługą błędów i walidacją
- Pięknym UI/UX z myślą o dostępności
- Optymalizacją wydajności i responsywnym designem
- Nowoczesnymi narzędziami deweloperskimi i najlepszymi praktykami

---

**Zbudowane z ❤️ używając React, Spring Boot i nowoczesnych technologii webowych.** 