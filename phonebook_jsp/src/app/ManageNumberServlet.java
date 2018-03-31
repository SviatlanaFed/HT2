package app;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;


public class ManageNumberServlet extends HttpServlet {
    // Идентификатор для сериализации/десериализации.
    private static final long serialVersionUID = 1L;

    // Основной объект, хранящий данные телефонной книги.
    private PhonebookNumber phonebookNumber;
    private Phonebook phonebook;


    public ManageNumberServlet()
    {
        // Вызов родительского конструктора.
        super();

        // Создание экземпляра телефонной книги.
        try
        {
            this.phonebookNumber = PhonebookNumber.getInstance();
            this.phonebook= new Phonebook();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    // Валидация номера телефона и генерация сообщения об ошибке в случае невалидных данных.
    private String validatePhoneNumber(Number number )
    {
        String error_message = "";

        if (!number.validatePhoneNumber(number.getPhoneNumber(),false))
        {
            error_message += "Номер телефона должн быть строкой от 2 до 50 символов из цифр, знаков +  -  #.<br />";
        }
        if (!number.validatePhoneNumber(number.getPhoneNumber(),true))
        {
            error_message += "Номер телефона должн быть строкой от 0 до 50 символов из цифр, знаков +  -  #.<br />";
        }

        return error_message;
    }

    // Реакция на GET-запросы.
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        request.setCharacterEncoding("UTF-8");
        // Хранилище параметров для передачи в JSP.
        HashMap<String,String> jsp_parameters = new HashMap<String,String>();
        HttpSession session=request.getSession();

        // Диспетчеры для передачи управления на разные JSP (разные представления (view)).
        RequestDispatcher dispatcher_for_manager_edit_number = request.getRequestDispatcher("/ManageNumberEdit.jsp");
        RequestDispatcher dispatcher_for_manager_number = request.getRequestDispatcher("/ManageNumber.jsp");
        RequestDispatcher dispatcher_for_manager_number_add = request.getRequestDispatcher("/ManageNumberAdd.jsp");
        RequestDispatcher dispatcher_for_manager_forward = request.getRequestDispatcher("/ManagePersonForward.jsp");

        // Действие (action) и идентификатор записи (id) над которой выполняется это действие.
        String action = request.getParameter("action");
        String id = request.getParameter("id");
        String ownerId=request.getParameter("owner");
        String fullName=this.phonebook.getPerson(ownerId).getSurname()+" "+this.phonebook.getPerson(ownerId).getName()
                +" "+this.phonebook.getPerson(ownerId).getMiddlename();

        // Если идентификатор и действие не указаны, мы находимся в состоянии
        // "просто показать список и больше ничего не делать".
        if ((action == null)&&(id == null))
        {
            request.setAttribute("jsp_parameters",jsp_parameters);
            request.setAttribute("owner",ownerId);

            dispatcher_for_manager_number.forward(request, response);
        }
        // Если же действие указано, то...
        else
        {
            switch (action) {
                // Добавление записи.
                case "add":
                    // Создание новой пустой записи о пользователе.
                    Number empty_number = new Number();
                    empty_number.setOwner(request.getParameter("owner"));

                    // Подготовка параметров для JSP.
                    jsp_parameters.put("current_action", "add");
                    jsp_parameters.put("next_action", "add_go");
                    jsp_parameters.put("next_action_label", "Сохранить");

                    // Установка параметров JSP.
                    request.setAttribute("number", empty_number);
                    request.setAttribute("jsp_parameters", jsp_parameters);
                    request.setAttribute("fullName", fullName);

                    // Передача запроса в JSP.
                    dispatcher_for_manager_number_add.forward(request, response);
                    break;
                // Редактирование записи.
                case "edit":
                    // Извлечение из телефонной книги информации о редактируемой записи.
                    Number editable_number = this.phonebookNumber.getPhoneNumber(id);

                    // Подготовка параметров для JSP.
                    jsp_parameters.put("current_action", "edit");
                    jsp_parameters.put("next_action", "edit_go");
                    jsp_parameters.put("next_action_label", "Сохранить номер");

                    // Установка параметров JSP.
                    request.setAttribute("number", editable_number);
                    request.setAttribute("owner",ownerId);
                    request.setAttribute("jsp_parameters", jsp_parameters);
                    request.setAttribute("fullName", fullName);

                    // Передача запроса в JSP.
                    dispatcher_for_manager_edit_number.forward(request, response);
                    break;

                // Удаление записи.
                case "delete":

                    // Если запись удалось удалить...
                    if (phonebookNumber.deletePhoneNumber(id))
                    {
                        jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
                        jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
                    }
                    // Если запись не удалось удалить (например, такой записи нет)...
                    else
                    {
                        jsp_parameters.put("current_action_result", "DELETION_FAILURE");
                        jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
                    }

                    // Установка параметров JSP.
                    Person editable_person = this.phonebook.getPerson(ownerId);
                    session.setAttribute("jsp_parameters", jsp_parameters);
                    request.setAttribute("person",editable_person);
                    request.setAttribute("fullName", fullName);


                    // Передача запроса в JSP.
                    response.sendRedirect("/?action=edit&id="+ownerId);
                    //request.getRequestDispatcher("ManagePersonEdit.jsp").forward(request, response);
                    break;
            }
        }
    }

    // Реакция на POST-запросы.
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
        // иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
        request.setCharacterEncoding("UTF-8");

        // В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
        // но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
        request.setAttribute("phonebook", this.phonebook);
        HttpSession session=request.getSession();
        // Хранилище параметров для передачи в JSP.
        HashMap<String,String> jsp_parameters = new HashMap<String,String>();

        // Действие (add_go, edit_go) и идентификатор записи (id) над которой выполняется это действие.
        String add_go = request.getParameter("add_go");
        String edit_go = request.getParameter("edit_go");
        String id = request.getParameter("id");
        Person editable_person = this.phonebook.getPerson(request.getParameter("owner"));
        // Добавление записи.
        if (add_go != null)
        {
            // Создание записи на основе данных из формы.
            Number new_number =new Number(request.getParameter("owner"),request.getParameter("number"));

            // Валидация номера.
            String error_message = this.validatePhoneNumber(new_number);

            // Если данные верные, можно производить добавление.
            if (error_message.equals(""))
            {

                // Если запись удалось добавить...
                if (this.phonebookNumber.addPhoneNumber(new_number))
                {
                    jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
                    jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
                }
                // Если запись НЕ удалось добавить...
                else
                {
                    jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
                    jsp_parameters.put("current_action_result_label", "Ошибка добавления");
                }


            }
            // Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
            else
            {
                // Подготовка параметров для JSP.
                jsp_parameters.put("current_action", "add");
                jsp_parameters.put("next_action", "add_go");
                jsp_parameters.put("next_action_label", "Добавить");
                jsp_parameters.put("error_message", error_message);
            }
            // Установка параметров JSP.
            session.setAttribute("jsp_parameters", jsp_parameters);
            request.setAttribute("person",editable_person);


            // Передача запроса в JSP.
            response.sendRedirect("/?action=edit&id="+editable_person.getId());
        }
        // Редактирование записи.
        if (edit_go != null)
        {
            // Получение записи и её обновление на основе данных из формы.
            Number updatable_number = this.phonebookNumber.getPhoneNumber(id);
            updatable_number.setPhoneNumber(request.getParameter("number"));

            // Валидация номера телефона.
            String error_message = this.validatePhoneNumber(updatable_number);

            // Если данные верные, можно производить добавление.
            if (error_message.equals(""))
            {

                // Если запись удалось обновить...
                if (this.phonebookNumber.updatePhoneNumber(updatable_number))
                {
                    jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
                    jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
                }
                // Если запись НЕ удалось обновить...
                else
                {
                    jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
                    jsp_parameters.put("current_action_result_label", "Ошибка обновления");
                }
            }
            // Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
            else {

                // Подготовка параметров для JSP.
                jsp_parameters.put("current_action", "edit");
                jsp_parameters.put("next_action", "edit_go");
                jsp_parameters.put("next_action_label", "Сохранить");
                jsp_parameters.put("error_message", error_message);
            }
            // Установка параметров JSP.
            session.setAttribute("jsp_parameters", jsp_parameters);
            request.setAttribute("person",editable_person);


            // Передача запроса в JSP.
            response.sendRedirect("/?action=edit&id="+editable_person.getId());
        }
        }
    }


