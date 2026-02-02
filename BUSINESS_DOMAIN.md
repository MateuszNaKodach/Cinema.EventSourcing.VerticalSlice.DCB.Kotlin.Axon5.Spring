# Domena biznesowa

Wymagania w niektórych aspektach zostały uproszczone bądź skomplikowane ze względów dydaktycznych i ograniczenia
czasowego.
Np. wiele sal w kinie nie wnosi dużo do klasy rozwiązywanych problemów, a zwiększa obciążenie kognitywne i czas pracy.
Na początek skupmy się na modelowaniu krytycznych procesów biznesowych, nie rozpatrujemy zarządzania kontami użytkowników.

## Wymagania klienta

### Podstawowe informacje o kinie

- Tworzymy oprogramowanie dla małego kina z jedną salą kinową
- Sala ma 10 rzędów i 10 miejsc w każdym rzędzie (numerowane od 0 do 9)
- Kino jest czynne codziennie od 9:00 do 21:00
- Ekipa sprzątająca wchodzi po zamknięciu kina

### Zarządzanie miejscami i seansami

- Repertuar jest planowany na dany dzień. Żaden film nie zaczyna się przed otwarciem kina i nie kończy po jego zamknięciu. 
- Miejsca w kinie rozpatrujemy zawsze w kontekście konkretnego seansu
- Można rezerwować miejsca na seans od momentu jego zaplanowania w repertuarze
- Jestem zwolennikiem gotówki, na razie nie przewidujemy wprowadzania zakupów on-line czy cennika biletów. Rezerwacja to
  co innego niż zakup biletu.
- Jeśli rezerwacja nie zostanie odebrana 15 minut przed seansem, to należy ją anulować.
- Można też blokować miejsca poza rezerwacją (np. gdy ktoś pobrudził miejsce i nie chcemy dopuszczać tam rezerwacji)
- Na każdy dzień ustalamy dzienny program kinowy - wszystkie seanse z informacją o tym, który film gramy, o jakiej
  godzinie i kiedy się kończy

### System notyfikacji

- Potrzebny jest system notyfikacji umożliwiający:
    - Wysyłanie powiadomień
    - Czytanie wiadomości
    - Oznaczanie "gwiazdką" jako ważne
- Jeśli przeczytam wiadomość przynajmniej 2 razy, ma się automatycznie oznaczyć jako ważna
- W przyszłości możemy użyć AI do decydowania, co jest ważne
- Notyfikacje mogą przyjść jako email lub SMS

### Śledzenie popularności seansów

- Chcę śledzić popularne seanse, aby móc planować program w kolejnych dniach
- Popularny seans to taki, gdzie w ciągu godziny zarezerwowano przynajmniej 2 miejsca
- Chcę dostawać notyfikacje o popularnych seansach

### System zgłoszeń problemów

- Użytkownicy mogą zgłaszać problemy w kinie
- Przykłady zgłoszeń: "Rozlana Coca-Cola na miejscu 2:2" lub "Niedziałający projektor" 

### Audyt

- A, no i oczywiście chcę mieć pełen przegląd co, gdzie, kiedy stało się w systemie. 