package org.example.bigdatahackathon.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bigdatahackathon.entity.Complaint;
import org.example.bigdatahackathon.entity.User;
import org.example.bigdatahackathon.repository.ComplaintRepository;
import org.example.bigdatahackathon.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        log.info("Initializing data...");
        
        // Проверяем есть ли пользователи
        if (userRepository.count() == 0) {
            initUsers();
        }
        
        // Проверяем есть ли жалобы
        if (complaintRepository.count() == 0) {
            initSampleComplaints();
        }
        
        log.info("Data initialization completed. Total complaints: {}", complaintRepository.count());
    }
    
    private void initUsers() {
        log.info("Creating default users...");
        
        // Создаем админа
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@bdh.kz");
        admin.setEnabled(true);
        admin.setRoles(Set.of(User.Role.ADMIN));
        userRepository.save(admin);
        
        // Создаем обычного пользователя
        User resident = new User();
        resident.setUsername("resident");
        resident.setPassword(passwordEncoder.encode("user123"));
        resident.setEmail("resident@bdh.kz");
        resident.setEnabled(true);
        resident.setRoles(Set.of(User.Role.USER));
        userRepository.save(resident);
        
        log.info("Default users created: admin, resident");
    }
    
    private void initSampleComplaints() {
        log.info("Creating sample complaints...");
        
        List<Complaint> complaints = new ArrayList<>();
        
        // Жалобы на казахском и русском
        complaints.add(createComplaint(
            "65 автобус өте ескі, іші лас, жолаушыларға орын жетпейді. Жүргізуші өте дөрекі және ережені бұзады",
            "65",
            "Автобус",
            "Момышұлы-Панфилов аялдамасы",
            "Жүргізуші",
            new String[]{"Автобус сапасы", "Жүргізуші мінез-құлқы", "Қауіпсіздік"},
            "Жоғары",
            43.238949,
            76.889709,
            5
        ));
        
        complaints.add(createComplaint(
            "Автобус 12 маршрут постоянно опаздывает на 20-30 минут. Сегодня ждал целый час!",
            "12",
            "Автобус",
            "Микрорайон Алатау",
            "Диспетчерская служба",
            new String[]{"Расписание", "Время ожидания"},
            "Орташа",
            43.210701,
            76.851348,
            3
        ));
        
        complaints.add(createComplaint(
            "Троллейбус №4 ішінде кондиционер жұмыс істемейді, жазда өте ыстық болады",
            "4",
            "Троллейбус",
            "Республика алаңы",
            "Қызмет көрсету бөлімі",
            new String[]{"Жабдық", "Жайлылық"},
            "Төмен",
            43.238293,
            76.945465,
            1
        ));
        
        complaints.add(createComplaint(
            "95 маршрут автобусында жүргізуші өте жылдам жүреді, тежегенде адамдар құлайды. Қауіпті!",
            "95",
            "Автобус",
            "Самал-2 шағын ауданы",
            "Жүргізуші",
            new String[]{"Қауіпсіздік", "Жүргізу стилі", "Жолаушы қауіпсіздігі"},
            "Өте жоғары",
            43.232908,
            76.867265,
            10
        ));
        
        complaints.add(createComplaint(
            "В автобусе 29 не работают двери, водитель их открывает вручную. Это опасно зимой",
            "29",
            "Автобус",
            "Проспект Назарбаева",
            "Технический отдел",
            new String[]{"Оборудование", "Безопасность"},
            "Жоғары",
            43.250651,
            76.927898,
            7
        ));
        
        complaints.add(createComplaint(
            "79 автобусында төлем терминалы жұмыс істемейді, тек қолма-қол ақша қабылдайды. Қолайсыз!",
            "79",
            "Автобус",
            "Сайран микрорайоны",
            "Қаржы қызметі",
            new String[]{"Төлем жүйесі", "Технология"},
            "Орташа",
            43.220551,
            76.851234,
            4
        ));
        

        
        complaints.add(createComplaint(
            "Троллейбус 1 маршрутта жүргізуші телефонмен сөйлесе жүргізеді, бұл өте қауіпті!",
            "1",
            "Троллейбус",
            "Әуезов көшесі",
            "Жүргізуші",
            new String[]{"Қауіпсіздік", "Жүргізуші мінез-құлқы", "Ереже бұзу"},
            "Өте жоғары",
            43.251526,
            76.928181,
            10
        ));
        
        complaints.add(createComplaint(
            "В автобусе 23 водитель курил во время движения, ужасный запах",
            "23",
            "Автобус",
            "Рынок Алтын Орда",
            "Жүргізуші",
            new String[]{"Жүргізуші мінез-құлқы", "Гигиена", "Ереже бұзу"},
            "Жоғары",
            43.314277,
            76.949409,
            8
        ));
        
        complaints.add(createComplaint(
            "54 автобус өте кеш келеді, кешкісін сағат 10-дан кейін мүлдем жоқ",
            "54",
            "Автобус",
            "Бостандық даңғылы",
            "Диспетчерская служба",
            new String[]{"Расписание", "Кешкі қатынас"},
            "Орташа",
            43.228557,
            76.945756,
            5
        ));
        
        complaints.add(createComplaint(
            "Автобус 120 ішінде музыка қатты қосылып тұр, басқа ештеңе естілмейді",
            "120",
            "Автобус",
            "Қалқаман микрорайоны",
            "Жүргізуші",
            new String[]{"Жайлылық", "Шу"},
            "Төмен",
            43.218329,
            76.620912,
            2
        ));
        
        complaints.add(createComplaint(
            "Троллейбус 19 маршрут өте сирек жүреді, 40 минут күтудеміз керек",
            "19",
            "Троллейбус",
            "Абай даңғылы - Алтынсарин көшесі",
            "Диспетчерская служба",
            new String[]{"Расписание", "Жиілік"},
            "Орташа",
            43.235689,
            76.899445,
            4
        ));
        
        complaints.add(createComplaint(
            "В автобусе 99 окна не закрываются, зимой очень холодно",
            "99",
            "Автобус",
            "Проспект Райымбека",
            "Технический отдел",
            new String[]{"Оборудование", "Комфорт"},
            "Орташа",
            43.243822,
            76.955742,
            5
        ));
        
        complaints.add(createComplaint(
            "85 автобус аялдамада тұрмай өтіп кетті, қолымды көтерсем де тоқтамады!",
            "85",
            "Автобус",
            "Қарасай батыр көшесі",
            "Жүргізуші",
            new String[]{"Жүргізуші мінез-құлқы", "Қызмет сапасы"},
            "Жоғары",
            43.262489,
            76.937733,
            7
        ));
        
        complaints.add(createComplaint(
            "Автобус 77 сиденья все порваны, торчат металлические части - можно пораниться",
            "77",
            "Автобус",
            "Станция метро Москва",
            "Технический отдел",
            new String[]{"Безопасность", "Состояние салона", "Оборудование"},
            "Жоғары",
            43.250845,
            76.891727,
            8
        ));
        
        complaints.add(createComplaint(
            "Маршрутка 303 жүргізушісі жолаушыларға айқайлап тұрады, өте дөрекі",
            "303",
            "Маршрутка",
            "Көктем-3",
            "Жүргізуші",
            new String[]{"Жүргізуші мінез-құлқы", "Этика"},
            "Орташа",
            43.210133,
            76.665321,
            6
        ));
        
        complaintRepository.saveAll(complaints);
        log.info("Created {} sample complaints", complaints.size());
    }
    
    private Complaint createComplaint(String rawText, String route, String object, 
                                     String place, String actor, String[] aspects, 
                                     String priority, double lat, double lon, int daysAgo) {
        Complaint complaint = new Complaint();
        complaint.setRawText(rawText);
        complaint.setRoute(route);
        complaint.setObject(object);
        complaint.setPlace(place);
        complaint.setActor(actor);
        complaint.setAspect(aspects);
        complaint.setPriority(priority);
        complaint.setStatus("NEW");
        complaint.setConfidence(0.85 + (Math.random() * 0.14)); // 0.85-0.99
        complaint.setCreatedBy("system");
        complaint.setLatitude(lat);
        complaint.setLongitude(lon);
        
        // Устанавливаем время в прошлом
        OffsetDateTime time = OffsetDateTime.now(ZoneOffset.UTC).minusDays(daysAgo).minusHours((int)(Math.random() * 12));
        complaint.setTime(time);
        complaint.setCreatedAt(time);
        complaint.setUpdatedAt(time);
        
        return complaint;
    }
}

