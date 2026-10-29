
package com.example.praticum.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(
        tableName = "student",
        foreignKeys = {
                @ForeignKey(
                        entity = StudentGroup.class,
                        parentColumns = "id",
                        childColumns = "group_id",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index(value = "matricule", unique = true),
                @Index(value = {"group_id", "group_order"}, unique = true)
        }
)
public class Student {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "student_id")
    private String studentId;

    @NonNull
    @ColumnInfo(name = "last_name")
    private String lastName;

    @NonNull
    @ColumnInfo(name = "first_name")
    private String firstName;

    @Nullable
    @ColumnInfo(name = "last_name_arabic")
    private String lastNameArabic;

    @Nullable
    @ColumnInfo(name = "first_name_arabic")
    private String firstNameArabic;

    @Nullable
    @ColumnInfo(name = "matricule")
    private String matricule;

    @ColumnInfo(name = "group_id")
    private int groupId;

    @ColumnInfo(name = "group_order")
    private int groupOrder;

    @Nullable
    @ColumnInfo(name = "email")
    private String email;

    @Nullable
    @ColumnInfo(name = "notes")
    private String notes;

    @Nullable
    @ColumnInfo(name = "card_image_uri")
    private String cardImageUri;

    public Student(
            @NonNull String lastName,
            @NonNull String firstName,
            int groupId,
            int groupOrder
    ) {
        this.studentId = UUID.randomUUID().toString();
        this.lastName = lastName;
        this.firstName = firstName;
        this.groupId = groupId;
        this.groupOrder = groupOrder;
    }

    @NonNull
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(@NonNull String studentId) {
        this.studentId = studentId;
    }

    @NonNull
    public String getLastName() {
        return lastName;
    }

    public void setLastName(@NonNull String lastName) {
        this.lastName = lastName;
    }

    @NonNull
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NonNull String firstName) {
        this.firstName = firstName;
    }

    @Nullable
    public String getLastNameArabic() {
        return lastNameArabic;
    }

    public void setLastNameArabic(@Nullable String lastNameArabic) {
        this.lastNameArabic = lastNameArabic;
    }

    @Nullable
    public String getFirstNameArabic() {
        return firstNameArabic;
    }

    public void setFirstNameArabic(@Nullable String firstNameArabic) {
        this.firstNameArabic = firstNameArabic;
    }

    @Nullable
    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(@Nullable String matricule) {
        this.matricule = matricule;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getGroupOrder() {
        return groupOrder;
    }

    public void setGroupOrder(int groupOrder) {
        this.groupOrder = groupOrder;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    @Nullable
    public String getNotes() {
        return notes;
    }

    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }

    @Nullable
    public String getCardImageUri() {
        return cardImageUri;
    }

    public void setCardImageUri(@Nullable String cardImageUri) {
        this.cardImageUri = cardImageUri;
    }

    @NonNull
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @NonNull
    public String getFullNameArabic() {
        String first = firstNameArabic == null ? "" : firstNameArabic;
        String last = lastNameArabic == null ? "" : lastNameArabic;
        return (first + " " + last).trim();
    }

    @NonNull
    public String getInitials() {
        String first = firstName.trim();
        String last = lastName.trim();

        String firstInitial = first.isEmpty() ? "" :
                first.substring(0, 1).toUpperCase();

        String lastInitial = last.isEmpty() ? "" :
                last.substring(0, 1).toUpperCase();

        return firstInitial + lastInitial;
    }
}
