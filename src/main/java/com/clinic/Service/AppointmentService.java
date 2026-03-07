package com.clinic.Service;

import com.clinic.Model.Appointment;
import com.clinic.Model.Doctor;
import com.clinic.Model.Notification;
import com.clinic.Model.Patient;
import com.clinic.Repository.AppointmentRepository;
import com.clinic.Repository.DoctorRepository;
import com.clinic.Repository.NotificationRepository;
import com.clinic.Repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {


    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final RealTimeNotificationService realTimeNotificationService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public Appointment bookAppointment(Appointment appointment) {
        Patient fullPatient = patientRepository.findById(appointment.getPatient().getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctor fullDoctor = doctorRepository.findById(appointment.getDoctor().getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        appointment.setPatient(fullPatient);
        appointment.setDoctor(fullDoctor);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        Notification notification = new Notification();
        notification.setDoctorId(String.valueOf(fullDoctor.getDoctorId()));

        if (savedAppointment.getClinic() != null) {
            notification.setClinicId(String.valueOf(savedAppointment.getClinic().getClinicId()));
        }

        notification.setTitle("New Appointment Booked!");
        notification.setMessage(
                fullPatient.getFullName() + " has booked an appointment for " +
                        savedAppointment.getAppointmentDate() + " at " + savedAppointment.getTimeSlot()
        );

        notificationRepository.save(notification);

        realTimeNotificationService.sendNotification(
                String.valueOf(fullDoctor.getDoctorId()),
                notification.getMessage()
        );

        return savedAppointment;
    }

    public List<Appointment> findAllAppointment() {
        List<Appointment> appointments = appointmentRepository.findAll();
        appointments.forEach(a -> {
            if(a.getPatient() != null) a.getPatient().getFullName();
            if(a.getDoctor() != null) a.getDoctor().getFullName();
        });
        return appointments;
    }
}
